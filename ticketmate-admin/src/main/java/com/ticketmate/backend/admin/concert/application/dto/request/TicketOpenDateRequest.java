package com.ticketmate.backend.admin.concert.application.dto.request;

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

  @Min(value = 1, message = "최대 예매 매수는 1 이상이여야 합니다")
  @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
  private Integer requestMaxCount; // 최대 예매 매수

  private Boolean isBankTransfer; // 무통장 입금 여부

  @NotNull(message = "ticketOpenType이 비어있습니다")
  private TicketOpenType ticketOpenType; // 선예매, 일반예매 타입
}
