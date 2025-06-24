package com.ticketmate.backend.domain.applicationform.service;

import static com.ticketmate.backend.domain.member.domain.constant.MemberType.AGENT;
import static com.ticketmate.backend.domain.member.domain.constant.MemberType.CLIENT;
import static com.ticketmate.backend.global.constant.ApplicationFormConstants.APPLICATION_FORM_MIN_REQUEST_COUNT;
import static com.ticketmate.backend.global.util.common.CommonUtil.enumToString;

import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDetailRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.HopeAreaRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationFormDetail;
import com.ticketmate.backend.domain.applicationform.domain.entity.HopeArea;
import com.ticketmate.backend.domain.applicationform.domain.entity.RejectionReason;
import com.ticketmate.backend.domain.applicationform.repository.ApplicationFormRepository;
import com.ticketmate.backend.domain.applicationform.repository.RejectionReasonRepository;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import com.ticketmate.backend.domain.chat.repository.ChatRoomRepository;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.service.ConcertService;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.domain.member.service.MemberService;
import com.ticketmate.backend.domain.notification.domain.dto.request.NotificationPayloadRequest;
import com.ticketmate.backend.domain.notification.service.FcmService;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.util.common.PageableUtil;
import com.ticketmate.backend.global.util.notification.NotificationUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationFormService {

  private final RejectionReasonRepository rejectionReasonRepository;
  private final ApplicationFormRepository applicationFormRepository;
  private final NotificationUtil notificationUtil;
  private final FcmService fcmService;
  private final MemberService memberService;
  private final MemberRepository memberRepository;
  private final ConcertRepository concertRepository;
  private final EntityMapper entityMapper;
  private final ChatRoomRepository chatRoomRepository;
  private final ConcertService concertService;

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
   *                sortField 정렬할 필드 (기본: created_date)
   *                sortDirection 정렬 방향 (기본: DESC)
   */
  @Transactional(readOnly = true)
  public Page<ApplicationFormFilteredResponse> filteredApplicationForm(ApplicationFormFilteredRequest request) {

    UUID clientId = request.getClientId();
    UUID agentId = request.getAgentId();
    UUID concertId = request.getConcertId();
    String applicationStatus = enumToString(request.getApplicationFormStatus());

    // clientId가 입력된 경우 의뢰인 검증
    if (clientId != null) {
      Member client = memberRepository.findById(request.getClientId())
          .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_TYPE));
      if (!client.getMemberType().equals(CLIENT)) {
        log.error("요청된 의뢰인 MemberType에 오류가 있습니다.");
        throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
      }
    }

    // agentId가 입력된 경우 대리인 검증
    if (agentId != null) {
      Member agent = memberRepository.findById(request.getAgentId())
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      if (!agent.getMemberType().equals(AGENT)) {
        log.error("요청된 대리자 MemberType에 오류가 있습니다.");
        throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
      }
    }

    // concertId가 입력된 경우 콘서트 검증
    if (concertId != null) {
      concertRepository.findById(request.getConcertId())
          .orElseThrow(() -> {
            log.error("요청된 값에 해당하는 콘서트를 찾을 수 없습니다.");
            return new CustomException(ErrorCode.CONCERT_NOT_FOUND);
          });
    }

    // Pageable 객체 생성 (PageableUtil 사용)
    Pageable pageable = PageableUtil.createPageable(
        request.getPageNumber(),
        request.getPageSize(),
        request.getSortField(),
        request.getSortDirection(),
        "created_date", "request_count"
    );

    Page<ApplicationForm> applicationFormPage = applicationFormRepository
        .filteredApplicationForm(
            clientId,
            agentId,
            concertId,
            applicationStatus,
            pageable
        );

    // 엔티티를 DTO로 변환하여 Page 객체로 매핑
    return applicationFormPage.map(entityMapper::toApplicationFormFilteredResponse);
  }

  /**
   * 대리 티켓팅 신청서 상세 조회
   *
   * @param applicationFormId 신청서 PK
   * @return 신청서 정보
   */
  @Transactional(readOnly = true)
  public ApplicationFormFilteredResponse getApplicationFormInfo(UUID applicationFormId) {
    // 데이터베이스 조회
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);
    return entityMapper.toApplicationFormFilteredResponse(applicationForm);
  }

  /**
   * 신청서 수정
   *
   * @param applicationFormId      신청서 PK
   * @param client                 의뢰인 (신청서 작성자)
   * @param applicationFormRequest applicationFormDetailRequestList 신청서 공연회차 List
   *                               ticketOpenType 선예매 / 일반예매
   */
  @Transactional
  public void editApplicationForm(UUID applicationFormId, ApplicationFormRequest applicationFormRequest, Member client) {

    // 이미 작성된 신청서 확인
    ApplicationForm applicationForm = applicationFormRepository.findById(applicationFormId)
        .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND));


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
    // 현재 회원이 '대리인' 인지 검증
    memberService.validateMemberType(agent, AGENT);

    // 거절사유가 기타일때 메모는 2글자 이상이여야 한다.
    if (request.getApplicationFormRejectedType().equals(ApplicationFormRejectedType.OTHER)
        && request.getOtherMemo().length() < 2) {
      throw new CustomException(ErrorCode.INVALID_MEMO_REQUEST);
    }

    // DB에서 신청서 조회
    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    // 메모 default 세팅 or '기타' 사유의 메모 세팅
    String memo = request.getApplicationFormRejectedType() == ApplicationFormRejectedType.OTHER
        ? request.getOtherMemo() : "none";

    // 이미 거절당했던 신청 폼인지 확인 후 거절사유 객체 세팅
    RejectionReason rejectionReason = rejectionReasonRepository.findByApplicationForm(applicationForm)
        .orElseGet(() -> {
          log.debug("기존 거절사유에 대한 도메인이 없습니다.");
          return RejectionReason.builder()
              .applicationForm(applicationForm)
              .build();
        });

    applicationForm.setApplicationFormStatus(ApplicationFormStatus.REJECTED);
    log.debug("신청서 상태 거절변경 : {}", applicationForm.getApplicationFormStatus());

    // 거절사유, 메모 세팅
    rejectionReason.setApplicationFormRejectedType(request.getApplicationFormRejectedType());
    rejectionReason.setOtherMemo(memo);

    // 새로운 객체 저장 or Update
    rejectionReasonRepository.save(rejectionReason);

    Member client = applicationForm.getClient();

    if (notificationUtil.existsFcmToken(client.getMemberId())) {
      // 알림 전송용 payload, 회원객체 세팅
      NotificationPayloadRequest payloadRequest = notificationUtil
          .rejectNotification(request.getApplicationFormRejectedType(), agent, memo);

      fcmService.sendNotification(client.getMemberId(), payloadRequest);
    }
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
    // 현재 회원이 '대리인' 인지 검증
    memberService.validateMemberType(agent, AGENT);

    ApplicationForm applicationForm = findApplicationFormById(applicationFormId);

    Member client = applicationForm.getClient();

    // 승인하는 대리인이 의뢰인이 신청한 대리인이 정말 맞는지
    if (!applicationForm.getAgent().getMemberId().equals(agent.getMemberId())) {
      throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
    }

    // 신청서는 현재 "대기" 상태의 신청서만 승인 받을 수 있습니다.
    if (!applicationForm.getApplicationFormStatus().equals(ApplicationFormStatus.PENDING)) {
      throw new CustomException(ErrorCode.INVALID_APPLICATION_FORM_STATUS);
    }

    /**
     * 이미 다른 대리자에 의해 신청서가 수락상태가 됐을 경우 수락 자체가 불가합니다.
     * 채팅방은 공연당 하나씩 생성합니다.
     * 선예매/일반예매는 각각 다른 공연으로 취급되어 한 공연당 2개의 채팅방이 존재할 수 있습니다.
     */

    // 신청서가 수락상태인지 검증
    boolean applicationFormExist = applicationFormRepository
        .existsByConcertConcertIdAndClientMemberIdAndTicketOpenTypeAndApplicationFormStatus(applicationForm.getConcert().getConcertId(),
            client.getMemberId(), applicationForm.getTicketOpenType(), ApplicationFormStatus.ACCEPTED);

    // 이미 수락된 신청서인지
    if (applicationFormExist) {
      throw new CustomException(ErrorCode.ALREADY_ACCEPTED_APPLICATION_FROM);
    }

    // 신청서 상태 승인 변경
    applicationForm.setApplicationFormStatus(ApplicationFormStatus.ACCEPTED);
    log.debug("신청서 상태 승인변경 : {}", applicationForm.getApplicationFormStatus());

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
              log.error("신청폼 조회에 실패했습니다.");
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
}