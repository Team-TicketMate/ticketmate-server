package com.ticketmate.backend.applicationform.application.dto.request;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.APPLICATION_FORM_MAX_REQUEST_COUNT;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.APPLICATION_FORM_MIN_REQUEST_COUNT;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;

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

  @NotNull(message = "performanceDate가 비어있습니다")
  private LocalDateTime performanceDate; // 공연일자

  @NotNull(message = "requestCount가 비어있습니다")
  @Min(value = APPLICATION_FORM_MIN_REQUEST_COUNT)
  @Max(value = APPLICATION_FORM_MAX_REQUEST_COUNT)
  private Integer requestCount; // 요청매수

  @Valid
  @Size(max = HOPE_AREA_MAX_SIZE, message = "희망 구역은 최대 5개까지 등록 가능합니다.")
  private List<HopeAreaRequest> hopeAreaList; // 희망구역 리스트

  @Size(max = 100, message = "요청사항은 최대 100자까지만 작성 가능합니다.")
  private String requirement; // 요청사항
}
