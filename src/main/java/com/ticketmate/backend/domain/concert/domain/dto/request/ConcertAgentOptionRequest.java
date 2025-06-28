package com.ticketmate.backend.domain.concert.domain.dto.request;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
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
public class ConcertAgentOptionRequest {
  private UUID concertId;

  private UUID agentId;

  private boolean accepting;
}
