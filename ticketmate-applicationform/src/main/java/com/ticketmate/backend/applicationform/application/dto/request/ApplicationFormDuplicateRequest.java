package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
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
public class ApplicationFormDuplicateRequest {

  @NotNull
  @NotNullErrorCode(ErrorCode.AGENT_ID_EMPTY)
  private UUID agentId;

  @NotNull
  @NotNullErrorCode(ErrorCode.CONCERT_ID_EMPTY)
  private UUID concertId;

  @NotNull
  @NotNullErrorCode(ErrorCode.TICKET_OPEN_TYPE_EMPTY)
  private TicketOpenType ticketOpenType;
}
