package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ApplicationFormDuplicateRequest {

  @NotNull(message = "대리인 PK 값을 입력하세요")
  private UUID agentId;

  @NotNull(message = "공연 PK 값을 입력하세요")
  private UUID concertId;

  @NotNull(message = "선예매/일반예매 타입을 입력하세요")
  private TicketOpenType ticketOpenType;
}
