package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotEmptyErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
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

  @NotNull
  @NotNullErrorCode(ErrorCode.AGENT_ID_EMPTY)
  private UUID agentId; // 대리인 PK

  @NotNull
  @NotNullErrorCode(ErrorCode.CONCERT_ID_EMPTY)
  private UUID concertId; // 콘서트 PK

  @Valid
  @NotEmpty
  @NotEmptyErrorCode(ErrorCode.APPLICATION_FORM_DETAIL_LIST_EMPTY)
  private List<ApplicationFormDetailRequest> applicationFormDetailRequestList; // 신청서 세부사항 List

  @NotNull
  @NotNullErrorCode(ErrorCode.TICKET_OPEN_TYPE_EMPTY)
  private TicketOpenType ticketOpenType; // 선예매/일반예매 여부
}
