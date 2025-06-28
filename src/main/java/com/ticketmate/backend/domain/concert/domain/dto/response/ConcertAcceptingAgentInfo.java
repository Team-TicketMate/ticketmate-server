package com.ticketmate.backend.domain.concert.domain.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConcertAcceptingAgentInfo {
  private UUID agentId;

  private String nickname;

  private String profileUrl;
}
