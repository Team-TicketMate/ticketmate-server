package com.ticketmate.backend.admin.concert.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
public class TicketOpenDateRequest {

  private LocalDateTime openDate; // 티켓 오픈일

  @Min(value = 1)
  @MinErrorCode(ErrorCode.REQUEST_MAX_COUNT_TOO_LOW)
  @Max(value = Integer.MAX_VALUE)
  @MaxErrorCode(ErrorCode.REQUEST_MAX_COUNT_TOO_HIGH)
  private Integer requestMaxCount; // 최대 예매 매수

  private Boolean isBankTransfer; // 무통장 입금 여부

  @NotNull
  @NotNullErrorCode(ErrorCode.TICKET_OPEN_TYPE_EMPTY)
  private TicketOpenType ticketOpenType; // 선예매, 일반예매 타입
}
