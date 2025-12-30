package com.ticketmate.backend.application.service;

import com.ticketmate.backend.common.core.event.fulfillmentform.FulfillmentFormEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuccessHistoryEventHandler {

  private final SuccessHistoryService successHistoryService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void createSuccessHistoryForEvent(FulfillmentFormEvent event) {
    successHistoryService.createSuccessHistory(event.fulfillmentFormId());
  }
}
