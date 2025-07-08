package com.ticketmate.backend.domain.applicationform.service;

import static com.ticketmate.backend.domain.member.domain.constant.MemberType.AGENT;
import static com.ticketmate.backend.domain.member.domain.constant.MemberType.CLIENT;
import static com.ticketmate.backend.global.constant.ApplicationFormConstants.APPLICATION_FORM_MIN_REQUEST_COUNT;
import static com.ticketmate.backend.global.constant.ApplicationFormConstants.EDITABLE_APPLICATION_FORM_STATUS;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormAction;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDetailRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormEditRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.HopeAreaRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormDetailResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationFormDetail;
import com.ticketmate.backend.domain.applicationform.domain.entity.HopeArea;
import com.ticketmate.backend.domain.applicationform.repository.ApplicationFormDetailRepositoryCustom;
import com.ticketmate.backend.domain.applicationform.repository.ApplicationFormRepository;
import com.ticketmate.backend.domain.applicationform.repository.ApplicationFormRepositoryCustom;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import com.ticketmate.backend.domain.chat.repository.ChatRoomRepository;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concert.service.ConcertService;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.service.MemberService;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayloadRequest;
import com.ticketmate.backend.domain.notification.service.FcmService;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final NotificationUtil notificationUtil;
  private final FcmService fcmService;
  private final MemberService memberService;
  private final EntityMapper entityMapper;
  private final ChatRoomRepository chatRoomRepository;
  private final ConcertService concertService;
  private final RejectionReasonService rejectionReasonService;

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
    ApplicationForm applicationForm = createApplicationFormEntity(client, agent, concert, ticketOpenDate, request.getTicketOpenType());

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
    return applicationFormRepositoryCustom
        .filteredApplicationForm(
            request.getClientId(),
            request.getAgentId(),
            request.getConcertId(),
            request.getApplicationFormStatusSet(),
            request.toPageable()
        );
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

    // 공연 상세정보 DTO (병렬처리)
    CompletableFuture<ConcertInfoResponse> concertInfoFuture =
        CompletableFuture.supplyAsync(() ->
            concertService.getConcertInfo(applicationForm.getConcert().getConcertId())
        );

    // 신청서 상세정보 DTO (병렬처리)
    CompletableFuture<List<ApplicationFormDetailResponse>> applicationFormDetailListFuture =
        CompletableFuture.supplyAsync(() ->
            getApplicationFormDetailResponseList(applicationFormId)
        );

    try {
      return ApplicationFormInfoResponse.builder()
          .concertInfoResponse(concertInfoFuture.get())
          .applicationFormDetailResponseList(applicationFormDetailListFuture.get())
          .build();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      log.error("신청서 상세 조회 병렬 처리 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * 신청서 수정 (대리인, 공연, 선예매/일반예매 변경불가)
   *
   * @param applicationFormId 신청서 PK
   * @param editRequest       신청서 수정 DTO
   * @param client            의뢰인 (신청서 작성자)
   */
  @Transactional
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

    // TODO: 의뢰인에게 신청서 거절 알림 발송 필요 (추후 알림로직 개편 후 작성예정)
  }

  /**
   * '대리인'의 신청서 승인
   *
   * @param applicationFormId 신청서 PK
   * @param agent             대리인 PK
   * @return 생성된 채팅방 PK
   */
  @Transactional
  public String acceptedApplicationForm(UUID applicationFormId, Member agent) {

    // DB에서 신청서 조회
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // '수락' 가능여부 검증
    validateApplicationFormAction(applicationForm, agent, ApplicationFormAction.ACCEPT);

    // TODO: 아래 로직 수정필요 -> 알림, 채팅 리팩토링 진행 시 수정
    /**
     * 이미 다른 대리자에 의해 신청서가 수락상태가 됐을 경우 수락 자체가 불가합니다.
     * 채팅방은 공연당 하나씩 생성합니다.
     * 선예매/일반예매는 각각 다른 공연으로 취급되어 한 공연당 2개의 채팅방이 존재할 수 있습니다.
     */

    Member client = applicationForm.getClient();
    // 신청서의 콘서트, 의뢰인, 대리인, 선예매/일반예매 필드를 참조해 이미 채팅방이 존재하는지 판별
    boolean chatRoomExist = chatRoomRepository
        .existsByAgentMemberIdAndClientMemberIdAndConcertIdAndTicketOpenType(agent.getMemberId(), client.getMemberId(),
            applicationForm.getConcert().getConcertId(), applicationForm.getTicketOpenType());

    // 존재한다면 에러 반환
    if (chatRoomExist) {
      throw new CustomException(ErrorCode.ALREADY_EXIST_CHAT_ROOM);
    }

    // 없으면 새로운 채팅방 생성
    ChatRoom chatRoom = ChatRoom.builder()
        .agentMemberId(agent.getMemberId())
        .agentMemberNickname(agent.getNickname())
        .clientMemberNickname(client.getNickname())
        .lastMessage("")
        .lastMessageId("")
        .clientMemberId(client.getMemberId())
        .applicationFormId(applicationFormId)
        .concertId(applicationForm.getConcert().getConcertId())
        .ticketOpenType(applicationForm.getTicketOpenType())
        .build();

    chatRoomRepository.save(chatRoom);

    // 알림전송
    if (notificationUtil.existsFcmToken(client.getMemberId())) {
      NotificationPayloadRequest payloadRequest = notificationUtil.approveNotification(agent);

      fcmService.sendNotification(client.getMemberId(), payloadRequest);
    }

    return chatRoom.getChatRoomId();
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
        .existsByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(
            client.getMemberId(),
            request.getAgentId(),
            request.getConcertId(),
            request.getTicketOpenType()
        );
  }

  /**
   * DB에서 applicationFormId에 해당하는 신청서를 찾고 반환합니다
   *
   * @param applicationFormId 신청서 PK
   * @return ApplicationForm
   */
  private ApplicationForm findApplicationFormById(UUID applicationFormId) {
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
    if (applicationFormRepository.existsByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(clientId, agentId, concertId, ticketOpenType)) {
      throw new CustomException(ErrorCode.DUPLICATE_APPLICATION_FROM_REQUEST);
    }
  }

  /**
   * DB에서 ApplicationFormDetail 엔티티를 조회해서 DTO로 변환
   *
   * @param applicationFormId 조회하려는 신청서 PK
   */
  private List<ApplicationFormDetailResponse> getApplicationFormDetailResponseList(UUID applicationFormId) {
    return Optional.of(applicationFormId)
        .map(applicationFormDetailRepositoryCustom::findAllApplicationFormDetailWithHopeAreaListByApplicationFormId)
        .map(entityMapper::toApplicationFormDetailResponseList)
        .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_FORM_DETAIL_NOT_FOUND));
  }

  /**
   * 신청서(ApplicationForm) 엔티티 생성
   */
  private ApplicationForm createApplicationFormEntity(Member client, Member agent, Concert concert, TicketOpenDate ticketOpenDate, TicketOpenType ticketOpenType) {
    return ApplicationForm.builder()
        .client(client)
        .agent(agent)
        .concert(concert)
        .ticketOpenDate(ticketOpenDate)
        .applicationFormDetailList(new ArrayList<>())
        .applicationFormStatus(ApplicationFormStatus.PENDING) // 신청서는 기본 '대기'상태
        .ticketOpenType(ticketOpenType)
        .build();
  }

  /**
   * 신청서 세부사항(ApplicationFormDetail) 엔티티 생성
   */
  private ApplicationFormDetail createApplicationFormDetailEntity(ApplicationFormDetailRequest detailRequest, ConcertDate concertDate) {
    ApplicationFormDetail applicationFormDetail = ApplicationFormDetail.builder()
        .concertDate(concertDate)
        .requestCount(detailRequest.getRequestCount())
        .requirement(detailRequest.getRequestDetails())
        .hopeAreaList(new ArrayList<>())
        .build();

    // 희망구역 설정
    if (!CommonUtil.nullOrEmpty(detailRequest.getHopeAreaList())) {
      for (HopeAreaRequest hopeAreaRequest : detailRequest.getHopeAreaList()) {
        HopeArea hopeArea = HopeArea.builder()
            .priority(hopeAreaRequest.getPriority())
            .location(hopeAreaRequest.getLocation())
            .price(hopeAreaRequest.getPrice())
            .build();
        applicationFormDetail.addHopeArea(hopeArea);
      }
    }

    return applicationFormDetail;
  }


  /**
   * 신청서 세부사항 요청 처리
   */
  private void processApplicationFormDetailRequestList(ApplicationForm applicationForm, List<ApplicationFormDetailRequest> detailRequestList, TicketOpenDate ticketOpenDate) {
    // 신청서 세부사항 공연일자 검증
    validatePerformanceDate(detailRequestList);

    for (ApplicationFormDetailRequest detailRequest : detailRequestList) {
      // 요청 매수 검증
      validateRequestCount(detailRequest, ticketOpenDate.getRequestMaxCount());

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
   * 신청서 세부사항 공연일자 검증
   *
   * @param requestList 신청서 세부사항 List
   */
  private void validatePerformanceDate(List<ApplicationFormDetailRequest> requestList) {
    // 신청서 세부사항은 최소1개 이상
    if (CommonUtil.nullOrEmpty(requestList)) {
      log.error("신청서에는 최소 1개 이상의 공연일자가 포함되어야 합니다.");
      throw new CustomException(ErrorCode.APPLICATION_FORM_DETAIL_REQUIRED);
    }

    // 공연일자 중복 검사
    Set<LocalDateTime> performanceDateSet = new HashSet<>();
    for (ApplicationFormDetailRequest request : requestList) {
      // 공연일자 null 검증
      if (request.getPerformanceDate() == null) {
        log.error("요청된 신청서 세부사항 공연일자가 null 입니다.");
        throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
      }
      // 중복 검사
      if (!performanceDateSet.add(request.getPerformanceDate())) {
        log.error("중복된 공연일자가 존재합니다: {}", request.getPerformanceDate());
        throw new CustomException(ErrorCode.DUPLICATE_CONCERT_DATE);
      }
    }
  }

  /**
   * 요청 매수 검증
   *
   * @param request         신청서 세부사항 dto
   * @param requestMaxCount 최대 예매 가능 매수
   */
  private void validateRequestCount(ApplicationFormDetailRequest request, int requestMaxCount) {
    if (request.getRequestCount() < APPLICATION_FORM_MIN_REQUEST_COUNT ||
        request.getRequestCount() > requestMaxCount) {
      log.error("대리 신청 매수는 최소 {}장, 최대 {}장까지 가능합니다. 요청된 예매 매수: {}",
          APPLICATION_FORM_MIN_REQUEST_COUNT, requestMaxCount, request.getRequestCount());
      throw new CustomException(ErrorCode.TICKET_REQUEST_COUNT_EXCEED);
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
      case REJECT, ACCEPT -> { // '거절', '수락'을 하려는 경우 (대리인만 가능)
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
        if (!EDITABLE_APPLICATION_FORM_STATUS.contains(currentStatus)) {
          log.error("수정 불가 상태의 신청서입니다. 신청서 상태: {}", currentStatus);
          throw new CustomException(ErrorCode.INVALID_APPLICATION_FORM_STATUS);
        }
      }
      case CANCEL, REJECT, ACCEPT -> {
        if (!currentStatus.equals(ApplicationFormStatus.PENDING)) {
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
    // 거절사유가 기타일때 메모는 2글자 이상이여야 한다.
    if (request.getApplicationFormRejectedType().equals(ApplicationFormRejectedType.OTHER)) {
      if (CommonUtil.nvl(request.getOtherMemo(), "").isEmpty() || request.getOtherMemo().length() < 2) {
        log.error("거절사유가 OTHER인 경우 메모는 2글자 이상 작성해야합니다. 요청된 메모: {}", request.getOtherMemo());
        throw new CustomException(ErrorCode.INVALID_MEMO_REQUEST);
      }
    }
  }
}