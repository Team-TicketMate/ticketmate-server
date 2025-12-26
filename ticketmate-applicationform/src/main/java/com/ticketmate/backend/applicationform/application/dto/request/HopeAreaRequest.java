package com.ticketmate.backend.applicationform.application.dto.request;


import static com.ticketmate.backend.common.core.constant.ValidationConstants.ApplicationForm.HOPE_AREA_MAX_SIZE;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.PositiveErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class HopeAreaRequest {

  @Min(value = 1)
  @MinErrorCode(ErrorCode.PRIORITY_RANGE_INVALID)
  @Max(value = HOPE_AREA_MAX_SIZE)
  @MaxErrorCode(ErrorCode.PRIORITY_RANGE_INVALID)
  private int priority; // 순위

  @NotBlank
  @NotBlankErrorCode(ErrorCode.LOCATION_EMPTY)
  private String location; // 구역

  @Positive
  @PositiveErrorCode(ErrorCode.PRICE_TOO_LOW)
  private int price; // 가격
}
