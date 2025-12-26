package com.ticketmate.backend.admin.concert.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
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

  @NotNull
  @NotNullErrorCode(ErrorCode.PERFORMANCE_DATE_EMPTY)
  private LocalDateTime performanceDate;

  @Min(value = 1)
  @MinErrorCode(ErrorCode.SESSION_TOO_LOW)
  @Max(value = Integer.MAX_VALUE)
  @MaxErrorCode(ErrorCode.SESSION_TOO_HIGH)
  private int session;
}
