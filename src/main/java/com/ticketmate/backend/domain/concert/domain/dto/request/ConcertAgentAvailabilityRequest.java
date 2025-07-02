package com.ticketmate.backend.domain.concert.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(defaultValue = "d11a7778-6e53-4235-855c-96288f8eeac5")
  private UUID concertId;

  @Schema(defaultValue = "true")
  private Boolean accepting = true;

  @Schema(defaultValue = "안녕하세요 저에게 맡겨주세요 !!!")
  private String introduction;
}
