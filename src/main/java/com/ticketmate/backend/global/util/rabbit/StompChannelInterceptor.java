package com.ticketmate.backend.global.util.rabbit;

import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.security.Principal;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 해당 컴포넌트는 WebSocket 연결 후 메시지를 주고 받을때 ChannelInterceptor에 대한 커스텀 구현체입니다.
 * 웹소켓 연결시의 사용자 인증/인가, 메시지를 주고 받을시 AT 검증을 수행합니다.
 * 구현방법중 AOP를 이용해 커스텀 에노테이션을 만들어 구현할 수 있지만 추후 확장이 필요할때 고민해보도록 하겠습니다.
 */

@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompChannelInterceptor implements ChannelInterceptor {

  private final JwtUtil jwtUtil;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null) {
      return message;
    }
    log.debug("preSend: accessor={}", accessor);

    StompCommand command = accessor.getCommand();
    if (command == null) {
      return message;
    }

    switch (command) {
      case CONNECT -> handleConnect(accessor);
      case SUBSCRIBE -> {
        checkPrincipalExpiration(accessor);
        handleSubscribe(accessor);
      }
      case SEND -> {
        checkPrincipalExpiration(accessor);
        handleMessage(accessor);
      }
      case MESSAGE -> {
        // 서버(브로커) → 클라이언트 메시지(Outbound)
        handleMessage(accessor);
      }
      default -> {
        // 그 외 명령은 별도 처리 없으면 패스
      }
    }

    return message;
  }

  private void handleConnect(StompHeaderAccessor accessor) {
    String authorization = accessor.getFirstNativeHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      String accessToken = authorization.substring(7);

      // 1) CONNECT 시 토큰 파싱
      long remaining = jwtUtil.getRemainingValidationMilliSecond(accessToken);
      if (remaining <= 0) {
        throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
      }

      // 2) CustomOAuth2User 생성 시 AT 만료시간을 세팅
      Authentication authentication = jwtUtil.getAuthentication(accessToken);
      CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
      customOAuth2User.confirmExpire(remaining);

      accessor.setUser(customOAuth2User);
    } else {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
  }

  private void checkPrincipalExpiration(StompHeaderAccessor accessor) {
    Principal principal = accessor.getUser();
    if (principal instanceof CustomOAuth2User customOAuth2User) {
      // SUB 혹은 SEND시 (클라이언트 요청) AT의 만료시간을 검증한다.
      log.debug("사용자 만료시간 정보 검증중");
      if (customOAuth2User.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
      }
    } else { // 잘못된 토큰이 들어올 경우
      throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
    }
  }

  /**
   * @param accessor 클라이언트가 채팅 서버에 대한 구독 요청시 기존의 /exchange/chat.exchange/ 경로로 구독하는 것이 아닌 /sub/ 경로로 구독하게 하는 로직입니다.
   *                 즉, 클라이언트측에서 /sub/chat.room.1 경로로 구독 요청이 들어온다면 서버에서 내부적으로 /exchange/chat.exchange/ 의 경로로 바꿔쳐 메시지 브로커에 등록합니다.
   *                 이는 RabbitMQ에 완전히 종속되있는 상태를 벗어나 추후 다른 MQ 브로커를 사용할 때의 편의성을 높여줍니다.
   *                 (사실 RabbitMQ만 사용할거면 해도 그만 안해도 그만압니다.)
   */
  private void handleSubscribe(StompHeaderAccessor accessor) {
    String destination = accessor.getDestination();
    log.debug("INBOUND SUBSCRIBE - 원래 구독 주소: {}", destination);
    if (destination != null && destination.startsWith("/sub/")) {
      String convertedDestination = convertToExchangeDestination(destination);
      log.debug("INBOUND SUBSCRIBE - 변경된 구독 주소: {}", convertedDestination);
      accessor.setDestination(convertedDestination);
    }
  }

  /**
   * @param accessor 위의 handleSubscribe 같은 느낌입니다.
   *                 위는 INBOUND 아래는 OUTBOUND인정도만 다릅니다.
   */
  private void handleMessage(StompHeaderAccessor accessor) {
    String destination = accessor.getDestination();
    log.debug("OUTBOUND MESSAGE - 원래 발행 주소: {}", destination);
    if (destination != null && destination.startsWith("/exchange/chat.exchange/")) {
      String convertedDestination = convertToSubDestination(destination);
      log.debug("OUTBOUND MESSAGE - 변경된 발행 주소: {}", convertedDestination);
      accessor.setDestination(convertedDestination);
    }
  }

  private String convertToExchangeDestination(String originalDestination) {
    return originalDestination.replace("/sub/", "/exchange/chat.exchange/");
  }

  private String convertToSubDestination(String originalDestination) {
    return originalDestination.replace("/exchange/chat.exchange/", "/sub/");
  }
}
