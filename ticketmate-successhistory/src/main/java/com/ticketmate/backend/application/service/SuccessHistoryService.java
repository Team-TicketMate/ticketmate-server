package com.ticketmate.backend.application.service;

import com.ticketmate.backend.application.dto.request.SuccessHistoryFilteredRequest;
import com.ticketmate.backend.application.dto.response.SuccessHistoryResponse;
import com.ticketmate.backend.application.mapper.SuccessHistoryMapper;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.infrastructure.entity.SuccessHistory;
import com.ticketmate.backend.infrastructure.repository.SuccessHistoryRepository;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuccessHistoryService {

  private final SuccessHistoryRepository successHistoryRepository;
  private final EntityManager entityManager;
  private final MemberService memberService;
  private final SuccessHistoryMapper mapper;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createSuccessHistory(UUID fulfillmentFormId) {
    if (successHistoryRepository.existsByFulfillmentForm_FulfillmentFormId(fulfillmentFormId)) {
      return;
    }

    FulfillmentForm fulfillmentForm = entityManager.getReference(FulfillmentForm.class, fulfillmentFormId);
    successHistoryRepository.save(SuccessHistory.create(fulfillmentForm));
  }

  @Transactional(readOnly = true)
  public Slice<SuccessHistoryResponse> getSuccessHistoryList(UUID agentId, SuccessHistoryFilteredRequest request) {
    Member agent = memberService.findMemberById(agentId);

    // 조회객체가 대리인이 정말 맞는지
    memberService.validateMemberType(agent, MemberType.AGENT);

    // 페이지네이션 객체 생성
    Pageable pageable = request.toPageable();

    return successHistoryRepository
      .findSuccessHistoryList(agentId, pageable)
      .map(mapper::toSuccessHistoryResponse);
  }
}