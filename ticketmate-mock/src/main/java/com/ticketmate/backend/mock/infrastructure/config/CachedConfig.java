package com.ticketmate.backend.mock.infrastructure.config;

import com.ticketmate.backend.mock.application.dto.response.MockChatRoomResponse;
import java.time.Clock;
import java.time.Instant;

/**
 * 1:1 채팅 테스트 전용 html에 토큰값을 고정시키기 위한 캐시 관련 설정클래스입니다.
 */
public record CachedConfig(String agentToken, String clientToken, String roomId, Instant expiresAt) {
  // 만료 60초 전부터 갱신
  public boolean isValid(Clock clock) { return expiresAt.isAfter(clock.instant().plusSeconds(60)); }
  public MockChatRoomResponse toResponse() {
    return MockChatRoomResponse.builder()
        .agentAccessToken(agentToken)
        .clientAccessToken(clientToken)
        .chatRoomId(roomId)
        .build();
  }
}
