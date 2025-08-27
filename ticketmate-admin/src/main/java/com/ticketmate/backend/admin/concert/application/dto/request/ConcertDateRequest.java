package com.ticketmate.backend.admin.concert.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertDateRequest {

  @NotNull(message = "공연일자를 입력해주세요")
  private LocalDateTime performanceDate;

  @Min(value = 1, message = "최대 예매 매수는 1 이상이여야 합니다")
  @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
  private int session;
}
