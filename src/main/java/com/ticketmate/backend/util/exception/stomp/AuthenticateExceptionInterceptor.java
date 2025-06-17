package com.ticketmate.backend.util.exception.stomp;

import com.ticketmate.backend.util.exception.CustomException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticateExceptionInterceptor implements StompExceptionInterceptor {

  @Override
  public boolean canHandle(Throwable cause) {
    return cause instanceof CustomException;
  }

  @Override
  public Message<byte[]> handle(Message<byte[]> clientMessage, Throwable cause) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
    accessor.setMessage("Custom error: " + cause.getMessage());
    accessor.setLeaveMutable(true);

    String payload = "CustomException 발생: " + cause.getMessage();
    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

    return MessageBuilder.createMessage(bytes, accessor.getMessageHeaders());
  }
}
