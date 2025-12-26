package com.ticketmate.backend.concertagentavailability.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
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

  @NotNull
  @NotNullErrorCode(ErrorCode.CONCERT_ID_EMPTY)
  private UUID concertId;

  private Boolean accepting = true;

  private String introduction;
}
