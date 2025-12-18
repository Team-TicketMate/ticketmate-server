package com.ticketmate.backend.applicationform.application.dto.request;

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

  @NotNull(message = "agentId가 비어있습니다")
  private UUID agentId;

  @NotNull(message = "concertId가 비어있습니다")
  private UUID concertId;

  @NotNull(message = "ticketOpenType이 비어있습니다")
  private TicketOpenType ticketOpenType;
}
