package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class ApplicationFormRequest {

  @NotNull(message = "agentId가 비어있습니다")
  private UUID agentId; // 대리인 PK

  @NotNull(message = "concertId가 비어있습니다")
  private UUID concertId; // 콘서트 PK

  @Valid
  @NotEmpty(message = "applicationFormDetailRequestList가 비어있습니다(최소 1개)")
  private List<ApplicationFormDetailRequest> applicationFormDetailRequestList; // 신청서 세부사항 List

  @NotNull(message = "ticketOpenType이 비어있습니다")
  private TicketOpenType ticketOpenType; // 선예매/일반예매 여부
}
