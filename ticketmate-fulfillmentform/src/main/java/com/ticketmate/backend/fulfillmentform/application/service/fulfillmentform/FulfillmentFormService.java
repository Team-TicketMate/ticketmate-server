package com.ticketmate.backend.fulfillmentform.application.service.fulfillmentform;

import static com.ticketmate.backend.common.core.util.CommonUtil.nullOrEmpty;
import static com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform.FulfillmentFormStatus.ACCEPTED_FULFILLMENT_FORM;
import static com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform.FulfillmentFormStatus.PENDING_FULFILLMENT_FORM;
import static com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform.FulfillmentFormStatus.REJECTED_FULFILLMENT_FORM;
import static com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform.FulfillmentFormStatus.UPDATE_FULFILLMENT_FORM;
import static com.ticketmate.backend.fulfillmentform.infrastructure.constant.FulfillmentFormConstants.UPDATABLE_STATUSES;

import com.ticketmate.backend.applicationform.application.service.ApplicationFormService;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.chat.application.service.ChatMessageService;
import com.ticketmate.backend.chat.application.service.ChatRoomService;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.event.fulfillmentform.FulfillmentFormEvent;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.request.FulfillmentFormInfoRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.request.FulfillmentFormRejectRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.request.FulfillmentFormUpdateRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.fulfillmentform.response.FulfillmentFormInfoResponse;
import com.ticketmate.backend.fulfillmentform.application.mapper.fulfillmentform.FulfillmentFormMapper;
import com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform.FulfillmentFormStatus;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.fulfillmentform.infrastructure.repository.fulfillmentform.FulfillmentFormRepository;
import com.ticketmate.backend.member.application.service.AgentBankAccountService;
import com.ticketmate.backend.member.application.service.AgentPerformanceService;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.AgentBankAccount;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.redis.application.annotation.RedisLock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillmentFormService {

  private final FulfillmentFormRepository fulfillmentFormRepository;
  private final FulfillmentFormImgService fulfillmentFormImgService;
  private final MemberService memberService;
  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;
  private final AgentBankAccountService agentBankAccountService;
  private final ApplicationFormService applicationFormService;
  private final FulfillmentFormMapper mapper;
  private final AgentPerformanceService agentPerformanceService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 티켓팅 성공시 대리인이 성공양식을 작성 및 저장하는 로직
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('fulfillment-form', #member.memberId, #chatRoomId)")
  public UUID saveFulfillmentFormInfo(Member member, String chatRoomId, FulfillmentFormInfoRequest request) {
    // 대리인 자격 검증
    memberService.validateMemberType(member, MemberType.AGENT);

    ChatRoom chatRoom = chatRoomService.findChatRoomById(chatRoomId);

    // 신청서 추출
    ApplicationForm applicationForm = applicationFormService.findApplicationFormById(chatRoom.getApplicationFormId());

    if (fulfillmentFormRepository.existsByChatRoomIdAndApplicationForm(chatRoomId, applicationForm)) {
      log.error("이미 해당 채팅방에 성공양식이 존재합니다.");
      throw new CustomException(ErrorCode.ALREADY_EXISTS_FULFILLMENT_FORM);
    }

    // 방 참가자 검증
    chatRoomService.validateRoomMember(chatRoom, member);

    // 선택된 대리인 계좌 추출
    AgentBankAccount agentBankAccount = agentBankAccountService.findAgentAccountById(request.getAgentBankAccountId());

    // 자신의 계좌가 맞는지 검증
    agentBankAccountService.validateAgentAccountOwner(agentBankAccount, member);

    // 의뢰인 추출
    Member client = applicationForm.getClient();

    // 콘서트 추출
    Concert concert = applicationForm.getConcert();

    // 성공양식 엔티티 생성
    FulfillmentForm fulfillmentFormForSave = FulfillmentForm.create(
      client,
      member,
      concert,
      applicationForm,
      chatRoomId,
      agentBankAccount,
      request.getParticularMemo(),
      PENDING_FULFILLMENT_FORM
    );

    // 이미지가 있다면 업로드 및 연관 관계 설정
    List<MultipartFile> imgList = request.getFulfillmentFormImgList();
    if (hasNonEmptyFileList(imgList)) {
      fulfillmentFormImgService.saveFulfillmentImgInfo(fulfillmentFormForSave, imgList);
    }

    // 저장 및 결과 검증
    FulfillmentForm fulfillmentForm = fulfillmentFormRepository.save(fulfillmentFormForSave);

    if (fulfillmentForm == null || fulfillmentForm.getFulfillmentFormId() == null) {
      throw new CustomException(ErrorCode.FULFILLMENT_FORM_UPLOAD_ERROR);
    }

    UUID fulfillmentFormId = fulfillmentForm.getFulfillmentFormId();

    // TODO 티켓팅이 성공했다는 알림 발송 로직 추가해야될듯
    // 트렌젝션 커밋 이후에만 호출되도록 보장 (채팅 발송은 채팅 메시지 서비스에서 수행)
    TransactionSynchronizationManager.registerSynchronization(
      new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          chatMessageService.sendFulfillmentFormMessage(chatRoomId, fulfillmentFormId, member);
        }
      });

    return fulfillmentFormId;
  }

  /**
   * 성공양식 조회 전용 로직
   */
  @Transactional(readOnly = true)
  public FulfillmentFormInfoResponse getFulfillmentFormInfo(Member member, UUID fulfillmentFormId) {
    FulfillmentForm fulfillmentForm = findFulfillmentFormById(fulfillmentFormId);

    ChatRoom chatRoom = chatRoomService.findChatRoomById(fulfillmentForm.getChatRoomId());

    // 방 참가자 검증
    chatRoomService.validateRoomMember(chatRoom, member);

    return mapper.toFulfillmentFormResponse(fulfillmentForm);
  }

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('fulfillment-form', #fulfillmentFormId)")
  public void acceptFulfillmentForm(Member member, UUID fulfillmentFormId) {
    FulfillmentForm fulfillmentForm = handleFulfillmentStatusForClient(member, fulfillmentFormId, ACCEPTED_FULFILLMENT_FORM);

    validateFulfillmentFormMember(member, fulfillmentForm, MemberType.CLIENT);

    eventPublisher.publishEvent(new FulfillmentFormEvent(fulfillmentFormId));

    // TODO 성공양식이 수락됐다는 알림 발송 로직 추가해야될듯

    try {
      // 대리인 통계 업데이트
      agentPerformanceService.addRecentSuccessStats(fulfillmentForm.getAgent());
    } catch (Exception e) {
      log.error("성공양식 수락 후 대리인 통계 업데이트에 실패했습니다. {}", e.getMessage(), e);
    }

    // 트렌젝션 커밋 이후에만 호출되도록 보장 (채팅 발송은 채팅 메시지 서비스에서 수행)
    TransactionSynchronizationManager.registerSynchronization(
      new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          chatMessageService.sendFulfillmentFormAcceptMessage(fulfillmentForm.getChatRoomId(), fulfillmentFormId, member);
        }
      });
  }

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('fulfillment-form', #fulfillmentFormId)")
  public void rejectFulfillmentForm(Member member, UUID fulfillmentFormId, FulfillmentFormRejectRequest request) {
    FulfillmentForm fulfillmentForm = handleFulfillmentStatusForClient(member, fulfillmentFormId, REJECTED_FULFILLMENT_FORM);

    validateFulfillmentFormMember(member, fulfillmentForm, MemberType.CLIENT);

    // TODO 성공양식이 거절됐다는 알림 발송 로직 추가해야될듯
    // 트렌젝션 커밋 이후에만 호출되도록 보장 (채팅 발송은 채팅 메시지 서비스에서 수행)
    TransactionSynchronizationManager.registerSynchronization(
      new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          chatMessageService.sendFulfillmentFormRejectMessage(fulfillmentForm.getChatRoomId(), fulfillmentFormId, member, request.getRejectedMemo());
        }
      });
  }

  /**
   * 대리인이 자신이 작성한 성공양식을 수정하는 로직
   */
  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('fulfillment-form', #fulfillmentFormId)")
  public void updateFulfillmentForm(Member member, UUID fulfillmentFormId, FulfillmentFormUpdateRequest request) {
    // 대리인 검증
    memberService.validateMemberType(member, MemberType.AGENT);

    FulfillmentForm fulfillmentForm = findFulfillmentFormById(fulfillmentFormId);

    validateFulfillmentFormMember(member, fulfillmentForm, MemberType.AGENT);

    // 수정 시도시 성공양식 상태 검증
    ensureUpdatableStatus(fulfillmentForm);

    // 새로운 이미지 + 삭제할 이미지가 있는지 판단
    boolean hasDelete = !nullOrEmpty(request.getDeleteImgIdList());
    boolean hasNew = !nullOrEmpty(request.getNewSuccessImgList());

    // 둘중 하나라도 있다면 진행
    if (hasDelete || hasNew) {
      fulfillmentFormImgService.updateImageListByDeleteAndAdd(fulfillmentForm, request.getDeleteImgIdList(), request.getNewSuccessImgList()
      );
    }

    // 상세설명 및 계좌 갱신: null이면 변경 없음
    if (request.getParticularMemo() != null) {
      fulfillmentForm.setParticularMemo(request.getParticularMemo());
    }

    if (request.getAgentBankAccountId() != null) {
      AgentBankAccount agentBankAccount = agentBankAccountService.findAgentAccountById(request.getAgentBankAccountId());
      agentBankAccountService.validateAgentAccountOwner(agentBankAccount, member);
      fulfillmentForm.setAgentBankAccount(agentBankAccount);
    }

    fulfillmentForm.setFulfillmentFormStatus(UPDATE_FULFILLMENT_FORM);

    TransactionSynchronizationManager.registerSynchronization(
      new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          // TODO 성공양식이 수정됐다는 알림 발송 로직 추가해야될듯
          chatMessageService.sendFulfillmentFormUpdatedMessage(
            fulfillmentForm.getChatRoomId(),
            fulfillmentForm.getFulfillmentFormId(), member);
        }
      });
  }

  /**
   * 이미지가 실제로 있는지 검증
   */
  private boolean hasNonEmptyFileList(List<MultipartFile> imgList) {
    return imgList != null && imgList.stream().anyMatch(f -> f != null && !f.isEmpty());
  }

  /**
   * 수정은 성공양식 거절 및 수락대기 상태에만 가능
   */
  private void ensureUpdatableStatus(FulfillmentForm fulfillmentForm) {
    FulfillmentFormStatus fulfillmentFormStatus = fulfillmentForm.getFulfillmentFormStatus();
    if (!UPDATABLE_STATUSES.contains(fulfillmentFormStatus)) {
      throw new CustomException(ErrorCode.FULFILLMENT_FORM_NOT_UPDATABLE);
    }
  }

  /**
   * 성공양식이 거절/수락인지 핸들링하는 공용 메서드(의뢰인 전용)
   */
  private FulfillmentForm handleFulfillmentStatusForClient(Member member, UUID fulfillmentFormId, FulfillmentFormStatus fulfillmentFormStatus) {
    // 의뢰인 검증
    memberService.validateMemberType(member, MemberType.CLIENT);

    FulfillmentForm fulfillmentForm = findFulfillmentFormById(fulfillmentFormId);

    switch (fulfillmentFormStatus) {
      case ACCEPTED_FULFILLMENT_FORM -> {
        fulfillmentForm.setFulfillmentFormStatus(ACCEPTED_FULFILLMENT_FORM);
        log.debug("의뢰인 성공양식 수락감지. 현재 상태 : {}", fulfillmentForm.getFulfillmentFormStatus());

        if (fulfillmentForm.getFulfillmentFormStatus() == ACCEPTED_FULFILLMENT_FORM) {
          log.error("이미 수락된 성공양식입니다.");
          throw new CustomException(ErrorCode.FULFILLMENT_FORM_ALREADY_ACCEPTED);
        }
      }

      case REJECTED_FULFILLMENT_FORM -> {
        // 이미 수락된 성공양식은 거절 불가
        if (fulfillmentForm.getFulfillmentFormStatus() == ACCEPTED_FULFILLMENT_FORM) {
          log.error("수락된 성공양식은 거절이 불가능합니다. 성공양식 ID : {}", fulfillmentForm.getFulfillmentFormId());
          throw new CustomException(ErrorCode.FULFILLMENT_FORM_ALREADY_ACCEPTED);
        }

        fulfillmentForm.setFulfillmentFormStatus(REJECTED_FULFILLMENT_FORM);
        log.debug("의뢰인 성공양식 거절감지. 현재 상태 : {}", fulfillmentForm.getFulfillmentFormStatus());
      }
    }

    return fulfillmentForm;
  }

  /**
   * ID를 통해 성공양식을 조회하는 메서드
   */
  public FulfillmentForm findFulfillmentFormById(UUID fulfillmentFormId) {
    return fulfillmentFormRepository.findById(fulfillmentFormId).orElseThrow(
      () -> {
        log.error("성공양식을 찾지 못했습니다. 요청받은 성공양식 ID: {}", fulfillmentFormId);
        throw new CustomException(ErrorCode.FULFILLMENT_FORM_NOT_FOUND);
      });
  }

  /**
   * 현재 요청한 사용자(의뢰인/대리인)가 성공양식에 매칭되어 있는지 검증
   */
  private void validateFulfillmentFormMember(Member member, FulfillmentForm fulfillmentForm, MemberType expected) {
    final UUID memberId = member.getMemberId();
    final UUID agentId = fulfillmentForm.getAgent() != null ? fulfillmentForm.getAgent().getMemberId() : null;
    final UUID clientId = fulfillmentForm.getClient() != null ? fulfillmentForm.getClient().getMemberId() : null;

    boolean isAgent = Objects.equals(agentId, memberId);
    boolean isClient = Objects.equals(clientId, memberId);

    switch (expected) {
      case AGENT -> {
        if (!isAgent) {
          throw new CustomException(ErrorCode.FULFILLMENT_MEMBER_NOT_AGENT);
        }
      }
      case CLIENT -> {
        if (!isClient) {
          throw new CustomException(ErrorCode.FULFILLMENT_MEMBER_NOT_CLIENT);
        }
      }
    }
  }
}
