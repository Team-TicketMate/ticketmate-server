package com.ticketmate.backend.concertagentavailability.application.dto.request;

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
@NoArgsConstructor
@AllArgsConstructor
public class ConcertAgentAvailabilityRequest {

  @NotNull(message = "concertId는 필수입니다.")
  private UUID concertId;

  private Boolean accepting = true;

  private String introduction;
}
