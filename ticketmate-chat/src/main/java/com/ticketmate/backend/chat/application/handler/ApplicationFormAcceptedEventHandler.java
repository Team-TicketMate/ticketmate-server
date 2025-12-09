package com.ticketmate.backend.chat.application.handler;

import com.ticketmate.backend.applicationform.core.evnet.ApplicationFormAcceptedEvent;
import com.ticketmate.backend.chat.application.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationFormAcceptedEventHandler {

  private final ChatRoomService chatRoomService;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void acceptedEventHandle(ApplicationFormAcceptedEvent event) {
    String chatRoomId = chatRoomService.generateChatRoom(event.applicationFormId());
    log.debug("신청서: {}에 대한 채팅방: {} 생성 완료", event.applicationFormId(), chatRoomId);
  }
}
