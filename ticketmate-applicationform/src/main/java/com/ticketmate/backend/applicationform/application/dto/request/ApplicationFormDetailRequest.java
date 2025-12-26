package com.ticketmate.backend.applicationform.application.dto.request;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.APPLICATION_FORM_MAX_REQUEST_COUNT;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.APPLICATION_FORM_MIN_REQUEST_COUNT;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
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
public class ApplicationFormDetailRequest {

  @NotNull
  @NotNullErrorCode(ErrorCode.PERFORMANCE_DATE_EMPTY)
  private LocalDateTime performanceDate; // 공연일자

  @NotNull
  @NotNullErrorCode(ErrorCode.REQUEST_COUNT_EMPTY)
  @Min(value = APPLICATION_FORM_MIN_REQUEST_COUNT)
  @Max(value = APPLICATION_FORM_MAX_REQUEST_COUNT)
  private Integer requestCount; // 요청매수

  @Valid
  @Size(max = HOPE_AREA_MAX_SIZE)
  @SizeErrorCode(ErrorCode.HOPE_AREA_LIST_SIZE_INVALID)
  private List<HopeAreaRequest> hopeAreaList; // 희망구역 리스트

  @Size(max = 100)
  @SizeErrorCode(ErrorCode.REQUIREMENT_TOO_LONG)
  private String requirement; // 요청사항
}
