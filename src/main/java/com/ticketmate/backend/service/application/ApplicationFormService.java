package com.ticketmate.backend.service.application;

import com.ticketmate.backend.object.constants.ApplicationFormRejectedType;
import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormRequest;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.object.dto.expressions.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.object.dto.notification.request.NotificationPayloadRequest;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.application.HopeArea;
import com.ticketmate.backend.object.postgres.application.RejectionReason;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import com.ticketmate.backend.repository.mongo.ChatRoomRepository;
import com.ticketmate.backend.repository.postgres.application.ApplicationFormRepository;
import com.ticketmate.backend.repository.postgres.application.expressions.RejectionReasonRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertDateRepository;
import com.ticketmate.backend.repository.postgres.concert.ConcertRepository;
import com.ticketmate.backend.repository.postgres.concert.TicketOpenDateRepository;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.service.fcm.FcmService;
import com.ticketmate.backend.util.common.EntityMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import com.ticketmate.backend.util.notification.NotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ticketmate.backend.object.constants.MemberType.AGENT;
import static com.ticketmate.backend.object.constants.MemberType.CLIENT;
import static com.ticketmate.backend.util.common.CommonUtil.enumToString;
import static com.ticketmate.backend.util.common.CommonUtil.null2ZeroInt;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationFormService {
    private final RejectionReasonRepository rejectionReasonRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final NotificationUtil notificationUtil;
    private final FcmService fcmService;
    private final MemberRepository memberRepository;
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final TicketOpenDateRepository ticketOpenDateRepository;
    private final EntityMapper entityMapper;
    private final ChatRoomRepository chatRoomRepository;

    private static final int MIN_REQUEST_COUNT = 1;

    /**
     * 대리자를 지정하여 공연 신청 폼을 작성합니다
     *
     * @param request agentId 대리인PK
     *                concertId 콘서트PK
     *                performanceDate 공연일자
     *                requestCount 요청매수
     *                hopeAreas 희망구역
     *                requestDetails 요청사항
     *                isPreOpen 선예매 여부
     */
    @Transactional
    public void createApplicationForm(ApplicationFormRequest request, Member client) {

        // 대리인 확인
        Member agent = memberRepository.findById(request.getAgentId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (!agent.getMemberType().equals(AGENT)) { // 해당 회원이 '대리인'이 아닌경우
            log.error("요청된 사용자는 대리인 자격이 없습니다. {}: {}", agent.getUsername(), agent.getMemberType());
            throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
        }

        // 의뢰인 확인
        if (!client.getMemberType().equals(CLIENT)) { // 해당 회원이 '의뢰인'이 아닌경우
            log.error("요청한 사용자는 의뢰인 자격이 없습니다. {}: {}", client.getUsername(), client.getMemberType());
            throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
        }

        // Concert 확인
        Concert concert = concertRepository.findById(request.getConcertId())
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        // 이미 의뢰인이 대리자에게 해당 공연으로 신청서를 보냈는지 확인
        if (applicationFormRepository.existsByClient_MemberIdAndAgent_MemberIdAndConcert_ConcertId(
                client.getMemberId(), agent.getMemberId(), concert.getConcertId())) {
            log.error("의뢰인: {} 이 대리인: {} 에게 이미 공연: {} 에 대해 신청서를 작성했습니다. 중복 작성은 불가능합니다.",
                    client.getMemberId(), agent.getMemberId(), concert.getConcertName());
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION_FROM_REQUEST);
        }

        // 공연PK + 공연일자로 ConcertDate엔티티 조회
        ConcertDate concertDate = concertDateRepository
                .findByConcertConcertIdAndPerformanceDate(concert.getConcertId(), request.getPerformanceDate())
                .orElseThrow(() -> {
                    log.error("공연: {} 공연일자: {} 에 해당하는 ConcertDate를 찾을 수 없습니다.",
                            concert.getConcertName(), request.getPerformanceDate());
                    return new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND);
                });

        // TicketOpenDate 확인
        TicketOpenDate ticketOpenDate = null;
        if (request.getIsPreOpen() != null) { // 선예매/일반예매 오픈일이 존재하는 경우
            log.debug("공연: {} 에 대한 신청서 요청에 선예매/일반예매 정보가 존재합니다.", concert.getConcertName());
            ticketOpenDate = ticketOpenDateRepository
                    .findByConcertConcertIdAndIsPreOpen(concert.getConcertId(), request.getIsPreOpen())
                    .orElseThrow(() -> {
                        log.error("공연: {} 에 해당하는 선예매/일반예매 정보를 찾을 수 없습니다.", concert.getConcertName());
                        return new CustomException(ErrorCode.TICKET_OPEN_DATE_NOT_FOUND);
                    });
        } else { // 선예매/일반예매 오픈일이 존재하지 않는 경우
            log.debug("공연: {} 에 대해 선예매/일반예매 정보가 존재하지 않습니다.", concert.getConcertName());
        }

        // 요청 매수 확인
        if (ticketOpenDate != null) {
            if (request.getRequestCount() < MIN_REQUEST_COUNT ||
                    request.getRequestCount() > ticketOpenDate.getRequestMaxCount()) {
                log.error("요청 매수는 최소 1장 최대 {}장까지 가능합니다. 요청된 예매 매수: {}",
                        ticketOpenDate.getRequestMaxCount(), request.getRequestCount());
                throw new CustomException(ErrorCode.TICKET_REQUEST_COUNT_EXCEED);
            }
        }

        // 희망구역 DTO List -> 엔티티 List 변환
        List<HopeArea> hopeAreaList = entityMapper
                .toHopeAreaList(request.getHopeAreaList());

        // ApplicationForm 생성 (hopeAreaList는 빈 상태로 초기화)
        ApplicationForm applicationForm = ApplicationForm.builder()
                .client(client)
                .agent(agent)
                .concert(concert)
                .concertDate(concertDate)
                .ticketOpenDate(ticketOpenDate)
                .requestCount(request.getRequestCount())
                .hopeAreaList(new ArrayList<>())
                .requestDetails(request.getRequestDetails())
                .applicationFormStatus(ApplicationFormStatus.PENDING) // 신청서는 기본 '대기'상태
                .build();

        // HopeArea 추가 및 양방향 관계 설정
        hopeAreaList.forEach(applicationForm::addHopeArea);

        applicationFormRepository.save(applicationForm);
        log.debug("요청된 신청서 저장 성공. 대리인: {}, 콘서트: {}", agent.getUsername(), concert.getConcertName());
    }

    /**
     * 신청서 필터링 조회
     *
     * @param request clientId 의뢰인 PK
     *                agentId 대리인 PK
     *                concertId 콘서트 PK
     *                requestCount 매수
     *                applicationStatus 신청서 상태
     *                pageNumber 요청 페이지 번호 (기본 0)
     *                pageSize 한 페이지 당 항목 수 (기본 30)
     *                sortField 정렬할 필드 (기본: created_date)
     *                sortDirection 정렬 방향 (기본: DESC)
     */
    @Transactional(readOnly = true)
    public Page<ApplicationFormFilteredResponse> filteredApplicationForm(ApplicationFormFilteredRequest request) {

        UUID clientId = request.getClientId();
        UUID agentId = request.getAgentId();
        UUID concertId = request.getConcertId();
        String applicationStatus = enumToString(request.getApplicationFormStatus());
        int requestCount = null2ZeroInt(request.getRequestCount());

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

        // 정렬 조건
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortField()
        );

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize(),
                sort
        );

        Page<ApplicationForm> applicationFormPage = applicationFormRepository
                .filteredApplicationForm(
                        clientId,
                        agentId,
                        concertId,
                        requestCount,
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
        ApplicationForm applicationForm = applicationFormRepository.findById(applicationFormId)
                .orElseThrow(() -> {
                    log.error("대리 티켓팅 신청서를 찾을 수 없습니다.");
                    return new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND);
                });

        return entityMapper.toApplicationFormFilteredResponse(applicationForm);
    }

    /**
     * 대리 티켓팅 신청서 거절
     *
     * @param applicationFormId 신청서 PK
     * @param agent 신청서 확인한 대리인
     * @param request   applicationRejectedType 거절 사유
     *                  otherMemo 거절 메모 (거절 사유가 '기타' 일 때)
     */
    @Transactional
    public void reject(UUID applicationFormId, Member agent, ApplicationFormRejectRequest request) {
        // 현재 회원이 '대리인'인가
        if (!agent.getMemberType().equals(MemberType.AGENT)) {
            throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
        }

        // 거절사유가 기타일때 메모는 2글자 이상이여야 한다.
        if (request.getApplicationFormRejectedType().equals(ApplicationFormRejectedType.OTHER)
                && request.getOtherMemo().length() < 2) {
            throw new CustomException(ErrorCode.INVALID_MEMO_REQUEST);
        }

        ApplicationForm applicationForm = applicationFormRepository.findById(applicationFormId).orElseThrow(
                () -> {
                    log.error("신청폼 조회에 실패했습니다.");
                    throw new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND);
                }
        );

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

        // 알림 전송용 payload, 회원객체 세팅
        NotificationPayloadRequest payloadRequest = notificationUtil.rejectNotification(request.getApplicationFormRejectedType(), agent, memo);
        Member client = applicationForm.getClient();

        fcmService.sendNotification(client.getMemberId(), payloadRequest);
    }

    @Transactional
    public String approve(UUID applicationFormId, Member agent) {
        // 현재 회원이 '대리인'인가
        if (!agent.getMemberType().equals(MemberType.AGENT)) {
            throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
        }

        ApplicationForm applicationForm = applicationFormRepository.findById(applicationFormId).orElseThrow(
                () -> {
                    log.error("신청폼 조회에 실패했습니다.");
                    throw new CustomException(ErrorCode.APPLICATION_FORM_NOT_FOUND);
                }
        );

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
         * 이미 다른 대리자에 의해 신청서가 수락상태가 됐을 경우 신청 자체가 불가합니다.
         * 추후 의뢰인은 한 콘서트당 하나의 신청폼을 작성해 매칭을 신청하는 부분을 고민합니다. (콘서트, 신청폼 1:1)
         */
        List<ApplicationForm> applicationFormList = applicationFormRepository
                .findAllByConcert_ConcertIdAndClient_MemberId(applicationForm.getConcert().getConcertId(), client.getMemberId());

        for (ApplicationForm form : applicationFormList) {
            if (form.getApplicationFormStatus().equals(ApplicationFormStatus.APPROVED)) {
                throw new CustomException(ErrorCode.ALREADY_APPROVED_APPLICATION_FROM);
            }
        }

        // 신청서 상태 승인 변경
        applicationForm.setApplicationFormStatus(ApplicationFormStatus.APPROVED);
        log.debug("신청서 상태 승인변경 : {}", applicationForm.getApplicationFormStatus());

        /*
         * 채팅방이 이미 존재한다면 해당 채팅방이 갖고있는 신청폼 정보만 바꿔줍니다.
         */
        // 이미 똑같은 대리인, 의뢰인의 채팅방이 존재하는지
        ChatRoom chatRoom = chatRoomRepository
                .findByAgentMemberIdAndClientMemberId(agent.getMemberId(), client.getMemberId())
                // 이미 존재한다면 기존 신청폼의 정보만 바꿉니다.
                .map(room -> {
                    log.debug("기존 채팅방 존재");
                    room.setApplicationFormId(applicationFormId);
                    return room;
                })
                // 없다면 새로운 채팅방을 생성합니다.
                .orElseGet(() ->
                        ChatRoom.builder()
                                .agentMemberId(agent.getMemberId())
                                .clientMemberId(client.getMemberId())
                                .applicationFormId(applicationFormId)
                                .build()
                );

        chatRoomRepository.save(chatRoom);

        // 알림전송
        NotificationPayloadRequest payloadRequest = notificationUtil.approveNotification(agent);
        fcmService.sendNotification(client.getMemberId(), payloadRequest);

        return chatRoom.getRoomId();
    }
}