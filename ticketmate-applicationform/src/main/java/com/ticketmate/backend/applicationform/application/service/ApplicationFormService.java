package com.ticketmate.backend.applicationform.application.service;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.APPLICATION_FORM_MIN_REQUEST_COUNT;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.REQUIREMENT_MAX_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberType.AGENT;
import static com.ticketmate.backend.member.core.constant.MemberType.CLIENT;

import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormDetailRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormEditRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.applicationform.application.dto.request.HopeAreaRequest;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormDetailResponse;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.applicationform.application.dto.view.ApplicationFormFilteredInfo;
import com.ticketmate.backend.applicationform.application.mapper.ApplicationFormMapper;
import com.ticketmate.backend.applicationform.application.validator.ApplicationFormDetailValidator;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormAction;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.core.evnet.ApplicationFormAcceptedEvent;
import com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail;
import com.ticketmate.backend.applicationform.infrastructure.entity.HopeArea;
import com.ticketmate.backend.applicationform.infrastructure.repository.ApplicationFormDetailRepositoryCustom;
import com.ticketmate.backend.applicationform.infrastructure.repository.ApplicationFormRepository;
import com.ticketmate.backend.applicationform.infrastructure.repository.ApplicationFormRepositoryCustom;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.service.ConcertService;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.application.type.ApplicationFormApproveNotificationType;
import com.ticketmate.backend.notification.application.type.ApplicationFormRejectNotificationType;
import com.ticketmate.backend.notification.core.service.NotificationService;
import com.ticketmate.backend.redis.application.annotation.RedisLock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationFormService {

  private final ApplicationFormRepository applicationFormRepository;
  private final ApplicationFormRepositoryCustom applicationFormRepositoryCustom;
  private final ApplicationFormDetailRepositoryCustom applicationFormDetailRepositoryCustom;
  private final ConcertService concertService;
  private final MemberService memberService;
  @Qualifier("web")
  private final NotificationService notificationService;
  private final ApplicationFormMapper applicationFormMapper;
  private final RejectionReasonService rejectionReasonService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 대리인를 지정하여 공연 신청 폼을 작성합니다
   * 선예매/일반예매가 다른 경우 각각 다른 공연으로 간주합니다
   * 하나의 신청서에는 여러개의 공연일자(회차)를 포함할 수 있습니다
   *
   * @param request agentId 대리인PK
   *                concertId 콘서트PK
   *                applicationFormDetailRequestList 신청서 세부사항 List
   *                ticketOpenType 선예매/일반예매 타입
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('application-form', #client.getMemberId(), #request.agentId, #request.concertId, #request.ticketOpenType)")
  public void createApplicationForm(ApplicationFormRequest request, Member client) {

    // 대리인 검증
    Member agent = memberService.findMemberById(request.getAgentId());
    memberService.validateMemberType(agent, AGENT);

    // 의뢰인 검증
    memberService.validateMemberType(client, CLIENT);

    // Concert 확인
    Concert concert = concertService.findConcertById(request.getConcertId());

    // 이미 의뢰인이 대리자에게 해당 공연(선예매/일반예매 구분)으로 신청서를 보냈는지 확인
    validateDuplicateApplicationForm(client.getMemberId(), agent.getMemberId(), concert.getConcertId(), request.getTicketOpenType());

    // TicketOpenDate 확인
    TicketOpenDate ticketOpenDate = concertService
      .findTicketOpenDateByConcertIdAndTicketOpenType(concert.getConcertId(), request.getTicketOpenType());

    // ApplicationForm 생성
    ApplicationForm applicationForm = ApplicationForm.create(client, agent, concert, ticketOpenDate, request.getTicketOpenType());

    // 신청서 세부사항 요청 처리
    processApplicationFormDetailRequestList(applicationForm, request.getApplicationFormDetailRequestList(), ticketOpenDate);

    applicationFormRepository.save(applicationForm);
  }

  /**
   * 신청서 필터링 조회
   *
   * @param request clientId 의뢰인 PK
   *                agentId 대리인 PK
   *                concertId 콘서트 PK
   *                applicationStatus 신청서 상태
   *                pageNumber 요청 페이지 번호 (기본 1)
   *                pageSize 한 페이지 당 항목 수 (기본 10)
   *                sortField 정렬할 필드 (기본: createdDate)
   *                sortDirection 정렬 방향 (기본: DESC)
   */
  @Transactional(readOnly = true)
  public Page<ApplicationFormFilteredResponse> filteredApplicationForm(ApplicationFormFilteredRequest request) {
    Page<ApplicationFormFilteredInfo> applicationFormFilteredInfoPage = applicationFormRepositoryCustom
      .filteredApplicationForm(
        request.getClientId(),
        request.getAgentId(),
        request.getConcertId(),
        request.getApplicationFormStatusSet(),
        request.toPageable()
      );
    return applicationFormFilteredInfoPage.map(applicationFormMapper::toApplicationFormFilteredResponse);
  }

  /**
   * 대리 티켓팅 신청서 상세 조회
   *
   * @param applicationFormId 신청서 PK
   * @return 신청서 정보
   */
  @Transactional(readOnly = true)
  public ApplicationFormInfoResponse getApplicationFormInfo(UUID applicationFormId) {

    // 신청서 조회
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // 공연 상세 조회
    ConcertInfoResponse concertInfoResponse = concertService.getConcertInfo(applicationForm.getConcert().getConcertId());

    // 신청서 세부사항 조회
    List<ApplicationFormDetailResponse> applicationFormDetailResponseList = getApplicationFormDetailResponseList(applicationFormId);

    return new ApplicationFormInfoResponse(
      concertInfoResponse,
      applicationFormDetailResponseList,
      applicationForm.getApplicationFormStatus(),
      applicationForm.getTicketOpenType()
    );
  }

  /**
   * 신청서 수정 (대리인, 공연, 선예매/일반예매 변경불가)
   *
   * @param applicationFormId 신청서 PK
   * @param editRequest       신청서 수정 DTO
   * @param client            의뢰인 (신청서 작성자)
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('application-form', #applicationFormId)")
  public void editApplicationForm(UUID applicationFormId, ApplicationFormEditRequest editRequest, Member client) {

    // 작성된 신청서 확인
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // '수정' 가능여부 검증
    validateApplicationFormAction(applicationForm, client, ApplicationFormAction.EDIT);

    // 기존 신청서 세부사항 삭제
    applicationForm.getApplicationFormDetailList().clear();

    // 신청서 세부사항 추가
    processApplicationFormDetailRequestList(applicationForm, editRequest.getApplicationFormDetailRequestList(), applicationForm.getTicketOpenDate());

    // 신청서 상태 "PENDING" 변경
    applicationForm.setApplicationFormStatus(ApplicationFormStatus.PENDING);
    applicationFormRepository.save(applicationForm);
  }

  /**
   * '의뢰인' 본인이 신청한 신청서 취소
   *
   * @param applicationFormId 취소하려는 신청서 PK
   * @param client            의뢰인
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('application-form', #applicationFormId)")
  public void cancelApplicationForm(UUID applicationFormId, Member client) {

    // 작성된 신청서 확인
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // '취소' 가능여부 검증
    validateApplicationFormAction(applicationForm, client, ApplicationFormAction.CANCEL);

    // 신청서 상태 "CANCELED" 변경
    applicationForm.setApplicationFormStatus(ApplicationFormStatus.CANCELED);
    applicationFormRepository.save(applicationForm);
  }

  /**
   * '대리인'의 신청서 거절
   *
   * @param applicationFormId 신청서 PK
   * @param agent             신청서 확인한 대리인
   * @param request           applicationRejectedType 거절 사유
   *                          otherMemo 거절 메모 (거절 사유가 '기타' 일 때)
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('application-form', #applicationFormId)")
  public void rejectApplicationForm(UUID applicationFormId, Member agent, ApplicationFormRejectRequest request) {

    // 거절 사유 및 메모 검증
    validateOtherMemo(request);

    // DB에서 신청서 조회
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // '거절' 가능여부 검증
    validateApplicationFormAction(applicationForm, agent, ApplicationFormAction.REJECT);

    // 신청서 상태 "REJECTED" 변경
    applicationForm.setApplicationFormStatus(ApplicationFormStatus.REJECTED);
    applicationFormRepository.save(applicationForm);

    // 거절 사유 저장 or 업데이트
    rejectionReasonService.saveOrUpdateRejectionReason(applicationForm, request);

    // 알림 페이로드 생성
    NotificationPayload payload = buildRejectNotificationPayload(agent, request);

    // 알림 발송
    notificationService.sendToMember(applicationForm.getClient().getMemberId(), payload);
  }

  /**
   * '대리인'의 신청서 승인
   *
   * @param applicationFormId 신청서 PK
   * @param agent             대리인 PK
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('application-form', #applicationFormId)")
  public void acceptedApplicationForm(UUID applicationFormId, Member agent) {

    // DB에서 신청서 조회
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // '수락' 가능여부 검증
    validateApplicationFormAction(applicationForm, agent, ApplicationFormAction.APPROVE);

    // 신청서 상태 "APPROVED" 변경
    applicationForm.setApplicationFormStatus(ApplicationFormStatus.APPROVED);
    applicationFormRepository.save(applicationForm);

    // TODO: 아래 로직 수정필요 -> 채팅 리팩토링 진행 시 수정
    /**
     * 이미 다른 대리자에 의해 신청서가 수락상태가 됐을 경우 수락 자체가 불가합니다.
     * 채팅방은 공연당 하나씩 생성합니다.
     * 선예매/일반예매는 각각 다른 공연으로 취급되어 한 공연당 2개의 채팅방이 존재할 수 있습니다.
     */

    // 신청서 수락 이벤트 발행
    ApplicationFormAcceptedEvent event = new ApplicationFormAcceptedEvent(applicationForm.getApplicationFormId());
    eventPublisher.publishEvent(event);

    // 알림전송
    NotificationPayload payload = buildApproveNotificationPayload(agent);

    notificationService.sendToMember(applicationForm.getClient().getMemberId(), payload);
  }

  /**
   * 특정 의뢰인이 이미 해당 공연, 해당 대리인, 일반/선 예매로 신청서를 작성했는지 여부를 반환합니다
   *
   * @param client  의뢰인 객체
   * @param request 대리인 PK, 공연 PK, 예매 타입
   * @return 중복된 신청서라면 true 반환
   */
  @Transactional(readOnly = true)
  public Boolean isDuplicateApplicationForm(Member client, ApplicationFormDuplicateRequest request) {
    return applicationFormRepository
      .findByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(
        client.getMemberId(),
        request.getAgentId(),
        request.getConcertId(),
        request.getTicketOpenType()
      ).isPresent();
  }

  /**
   * DB에서 applicationFormId에 해당하는 신청서를 찾고 반환합니다
   *
   * @param applicationFormId 신청서 PK
   * @return ApplicationForm
   */
  public ApplicationForm findApplicationFormById(UUID applicationFormId) {
    return applicationFormRepository.findById(applicationFormId)
      .orElseThrow(() -> {
          log.error("신청서를 찾을 수 없습니다. 요청PK: {}", applicationFormId);
          return new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND);
        }
      );
  }

  /**
   * 중복 신청서 검증
   *
   * @param clientId       의뢰인PK
   * @param agentId        대리인PK
   * @param concertId      공연PK
   * @param ticketOpenType 선예매/일반예매
   */
  private void validateDuplicateApplicationForm(UUID clientId, UUID agentId, UUID concertId, TicketOpenType ticketOpenType) {
    applicationFormRepository.findByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(
      clientId, agentId, concertId, ticketOpenType).ifPresent(applicationForm -> {
      log.error("중복된 신청서 요청입니다. 신청서 PK: {}", applicationForm.getApplicationFormId());
      throw new CustomException(ErrorCode.DUPLICATE_APPLICATION_FROM_REQUEST);
    });
  }

  /**
   * DB에서 ApplicationFormDetail 엔티티를 조회해서 DTO로 변환
   *
   * @param applicationFormId 조회하려는 신청서 PK
   */
  private List<ApplicationFormDetailResponse> getApplicationFormDetailResponseList(UUID applicationFormId) {
    List<ApplicationFormDetail> applicationFormDetailList = applicationFormDetailRepositoryCustom
      .findAllApplicationFormDetailWithHopeAreaListByApplicationFormId(applicationFormId);
    return applicationFormMapper.toApplicationFormDetailResponseList(applicationFormDetailList);
  }

  /**
   * 신청서 세부사항(ApplicationFormDetail) 엔티티 생성
   */
  private ApplicationFormDetail createApplicationFormDetailEntity(ApplicationFormDetailRequest detailRequest, ConcertDate concertDate) {
    ApplicationFormDetail applicationFormDetail = ApplicationFormDetail
      .create(concertDate, detailRequest.getRequestCount(), detailRequest.getRequirement());

    // 희망구역 설정
    if (!CommonUtil.nullOrEmpty(detailRequest.getHopeAreaList())) {
      for (HopeAreaRequest hopeAreaRequest : detailRequest.getHopeAreaList()) {
        HopeArea hopeArea = HopeArea.create(
          applicationFormDetail,
          hopeAreaRequest.getPriority(),
          hopeAreaRequest.getLocation(),
          hopeAreaRequest.getPrice()
        );
        applicationFormDetail.addHopeArea(hopeArea);
      }
    }
    return applicationFormDetail;
  }


  /**
   * 신청서 세부사항 요청 처리
   */
  private void processApplicationFormDetailRequestList(ApplicationForm applicationForm, List<ApplicationFormDetailRequest> detailRequestList, TicketOpenDate ticketOpenDate) {
    // 신청서 세부사항 검증
    ApplicationFormDetailValidator
      .of(detailRequestList)
      .performanceDateNonNullAndDistinct()
      .requestCountRange(APPLICATION_FORM_MIN_REQUEST_COUNT, ticketOpenDate.getRequestMaxCount())
      .requirementMaxLength(REQUIREMENT_MAX_LENGTH)
      .hopeAreaList(HOPE_AREA_MAX_SIZE);

    for (ApplicationFormDetailRequest detailRequest : detailRequestList) {
      // 공연일자 엔티티 조회
      ConcertDate concertDate = concertService.findConcertDateByConcertIdAndPerformanceDate(
        applicationForm.getConcert().getConcertId(), detailRequest.getPerformanceDate()
      );

      // ApplicationFormDetail 생성
      ApplicationFormDetail applicationFormDetail = createApplicationFormDetailEntity(detailRequest, concertDate);

      // ApplicationForm에 ApplicationFormDetail 추가
      applicationForm.addApplicationFormDetail(applicationFormDetail);
    }
  }

  /**
   * 액션에 따른 검증
   *
   * @param applicationForm 신청서
   * @param member          로그인된 사용자
   * @param action          신청서 상태를 변경하려는 액션
   */
  private void validateApplicationFormAction(ApplicationForm applicationForm, Member member, ApplicationFormAction action) {
    switch (action) {
      case EDIT, CANCEL -> { // '수정', '취소'를 하려는 경우 (의뢰인만 가능)
        memberService.validateMemberType(member, CLIENT); // 의뢰인 검증
        validateApplicationFormOwner(applicationForm, member); // 본인 소유 신청서 검증
        validateApplicationFormStatus(applicationForm, action); // 신청서 상태 검증
      }
      case REJECT, APPROVE -> { // '거절', '수락'을 하려는 경우 (대리인만 가능)
        memberService.validateMemberType(member, AGENT); // 대리인 검증
        validateAgentForApplicationForm(applicationForm, member); // 요청받은 신청서인지 검증
        validateApplicationFormStatus(applicationForm, action); // 신청서 상태 검증
      }
    }
  }

  /**
   * 신청서 변경 가능 상태 검증
   * [수정]: '취소', '진행취소', '거절' 상태의 신청서만 가능
   * [취소, 거절, 수락]: '대기' 상태의 신청서만 가능
   *
   * @param applicationForm 신청서
   * @param action          신청서 상태를 변경하려는 액션
   */
  private void validateApplicationFormStatus(ApplicationForm applicationForm, ApplicationFormAction action) {
    ApplicationFormStatus currentStatus = applicationForm.getApplicationFormStatus();

    switch (action) {
      case EDIT -> {
        if (!ApplicationFormConstants.EDITABLE_APPLICATION_FORM_STATUS.contains(currentStatus)) {
          log.error("수정 불가 상태의 신청서입니다. 신청서 상태: {}", currentStatus);
          throw new CustomException(ErrorCode.INVALID_APPLICATION_FORM_STATUS);
        }
      }
      case CANCEL, REJECT, APPROVE -> {
        if (currentStatus != ApplicationFormStatus.PENDING) {
          log.error("{} 불가 상태의 신청서입니다. 신청서 상태: {}", action.getDescription(), currentStatus);
          throw new CustomException(ErrorCode.INVALID_APPLICATION_FORM_STATUS);
        }
      }
    }
  }

  /**
   * 신청서 소유자 검증
   *
   * @param applicationForm 신청서
   * @param client          요청된 의뢰인
   */
  private void validateApplicationFormOwner(ApplicationForm applicationForm, Member client) {
    // 본인 소유 신청서 검증
    if (!applicationForm.getClient().getMemberId().equals(client.getMemberId())) {
      log.error("본인이 작성한 신청서만 수정이 가능합니다. 신청서 소유자: {}, 요청된 의뢰인: {}",
        applicationForm.getClient().getMemberId(), client.getMemberId());
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }
  }

  /**
   * 해당 대리인에게 작성된 신청서가 맞는지 검증
   *
   * @param applicationForm 신청서
   * @param agent           요청받은 대리인
   */
  private void validateAgentForApplicationForm(ApplicationForm applicationForm, Member agent) {
    // 신청서가 해당 대리인에게 작성되었는지 검증
    if (!applicationForm.getAgent().getMemberId().equals(agent.getMemberId())) {
      log.error("요청받은 대리인만 신청서 수락/거절이 가능합니다. 신청서 대상 대리인: {}, 요청된 대리인: {}",
        applicationForm.getAgent().getMemberId(), agent.getMemberId());
      throw new CustomException(ErrorCode.ACCESS_DENIED);
    }
  }

  /**
   * 거절 사유 검증
   *
   * @param request 거절 사유 및 메모
   */
  private void validateOtherMemo(ApplicationFormRejectRequest request) {
    // 거절사유가 기타일때 기타 메모는 2글자 이상
    if (request.getApplicationFormRejectedType().equals(ApplicationFormRejectedType.OTHER)) {
      if (CommonUtil.nvl(request.getOtherMemo(), "").isEmpty() || request.getOtherMemo().length() < 2) {
        log.error("거절사유가 OTHER인 경우 메모는 2글자 이상 작성해야합니다. 요청된 메모: {}", request.getOtherMemo());
        throw new CustomException(ErrorCode.INVALID_MEMO_REQUEST);
      }
    }
  }

  /**
   * 신청서 수락 알림 payload 생성
   */
  private NotificationPayload buildApproveNotificationPayload(Member agent) {
    return ApplicationFormApproveNotificationType.APPROVE.toPayload(agent.getNickname());
  }

  /**
   * 신청서 거절 알림 payload 생성
   */
  private NotificationPayload buildRejectNotificationPayload(Member agent, ApplicationFormRejectRequest request) {

    ApplicationFormRejectNotificationType notificationType =
      CommonUtil.stringToEnum(
        ApplicationFormRejectNotificationType.class,
        request.getApplicationFormRejectedType().name()
      );
    return notificationType.toPayload(agent.getNickname(), request.getOtherMemo());
  }
}