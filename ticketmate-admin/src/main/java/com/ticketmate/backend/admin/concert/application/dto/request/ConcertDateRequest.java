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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDateRequest {

  @NotNull(message = "performanceDate가 비어있습니다")
  private LocalDateTime performanceDate;

  @Min(value = 1, message = "session 값은 1 이상이여야 합니다")
  @Max(value = Integer.MAX_VALUE, message = "sessino 값은 정수 최대 범위를 넘을 수 없습니다.")
  private int session;
}
