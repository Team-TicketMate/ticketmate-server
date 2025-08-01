package com.ticketmate.backend.concert.application.dto.response;

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
public class ConcertAcceptingAgentResponse {

  private UUID agentId;

  private String nickname;

  private String profileUrl;

  private String introduction;

  private double averageRating;

  private int reviewCount;
}
