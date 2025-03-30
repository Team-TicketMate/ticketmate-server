package com.ticketmate.backend.util.exception.stomp;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompExceptionHandler extends StompSubProtocolErrorHandler {
    private final List<StompExceptionInterceptor> interceptors;

    @Override
    @Nullable
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = ex.getCause();
        log.error("웹소켓 관련 에러 발생 : {}", ex.getMessage());

        for (StompExceptionInterceptor interceptor : interceptors) {
            if (interceptor.canHandle(cause)) {
                log.error("STOMP client message processing error - 인터셉터 처리", cause);
                return interceptor.handle(clientMessage, cause);
            }
        }

        // 핸들링 할 수 없는 에러발생시
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(cause.getMessage());   // 클라이언트에서 확인할 수 있는 에러 메시지
        accessor.setLeaveMutable(true);

        String errorPayload = "핸들링 불가능한 예외 발생: " + cause.getMessage();
        byte[] bytes = errorPayload.getBytes(StandardCharsets.UTF_8);

        log.error("STOMP client message processing error", ex);
        return MessageBuilder.createMessage(bytes, accessor.getMessageHeaders());
    }
}