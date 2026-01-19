package com.ticketmate.backend.fulfillmentform.application.service.successhistory;

import com.ticketmate.backend.fulfillmentform.application.dto.successhistory.request.SuccessHistoryFilteredRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.successhistory.response.SuccessHistoryResponse;
import com.ticketmate.backend.fulfillmentform.application.mapper.successhistory.SuccessHistoryMapper;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.SuccessHistory;
import com.ticketmate.backend.fulfillmentform.infrastructure.repository.successhistory.SuccessHistoryRepository;
import com.ticketmate.backend.member.application.service.MemberService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuccessHistoryService {

  private final SuccessHistoryRepository successHistoryRepository;
  private final EntityManager entityManager;
  private final MemberService memberService;
  private final SuccessHistoryMapper mapper;

  public void createSuccessHistory(UUID fulfillmentFormId) {
    if (successHistoryRepository.existsByFulfillmentForm_FulfillmentFormId(fulfillmentFormId)) {
      log.debug("성공내역이 이미 존재합니다.");
      return;
    }

    FulfillmentForm fulfillmentForm = entityManager.getReference(FulfillmentForm.class, fulfillmentFormId);
    successHistoryRepository.save(SuccessHistory.create(fulfillmentForm));
  }

  @Transactional(readOnly = true)
  public Slice<SuccessHistoryResponse> getSuccessHistoryList(UUID agentId, SuccessHistoryFilteredRequest request) {
    // 페이지네이션 객체 생성
    Pageable pageable = request.toPageable();

    return successHistoryRepository
      .findSuccessHistoryList(agentId, pageable)
      .map(mapper::toSuccessHistoryResponse);
  }
}