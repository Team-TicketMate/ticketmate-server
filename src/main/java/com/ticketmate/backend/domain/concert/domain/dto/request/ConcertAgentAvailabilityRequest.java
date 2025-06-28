package com.ticketmate.backend.domain.concert.domain.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class ConcertAgentAvailabilityRequest {
  @NotNull(message = "concertId는 필수입니다.")
  private UUID concertId;

  @NotNull(message = "agentId는 필수입니다.")
  private UUID agentId;

  private boolean accepting;

  private String introduction;
}
