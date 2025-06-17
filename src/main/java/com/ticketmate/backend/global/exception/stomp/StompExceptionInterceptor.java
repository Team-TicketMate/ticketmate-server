package com.ticketmate.backend.global.exception.stomp;


import jakarta.annotation.Nullable;
import org.springframework.messaging.Message;

public interface StompExceptionInterceptor {

  /**
   * 해당 예외를 처리할 수 있는지 여부를 반환하는 메서드
   *
   * @return true: 해당 예외를 처리할 수 있음, false: 해당 예외를 처리할 수 없음
   */
  boolean canHandle(Throwable ex);

  /**
   * 예외를 처리하는 메서드.
   * WebSocket 프로토콜에 의해 ERROR 커맨드를 사용하면, client와의 연결을 반드시 끊어야 한다.
   *
   * @param clientMessage {@link Message}: client로부터 받은 메시지
   * @param ex            Throwable: 발생한 예외
   * @return {@link Message}: client에게 보낼 최종 메시지
   */
  @Nullable
  Message<byte[]> handle(@Nullable Message<byte[]> clientMessage, Throwable ex);
}
