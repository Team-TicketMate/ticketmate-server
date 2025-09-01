package com.ticketmate.backend.mock.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class MockChatRoomResponse {
  private String agentAccessToken;
  private String clientAccessToken;
  private String chatRoomId;
}
