package com.ticketmate.backend.applicationform.application.dto.request;

import com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants;
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
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormDetailRequest {

  @NotNull(message = "공연일자를 입력하세요")
  private LocalDateTime performanceDate; // 공연일자

  @NotNull(message = "티켓 요청 매수를 입력해주세요")
  @Min(value = ApplicationFormConstants.APPLICATION_FORM_MIN_REQUEST_COUNT)
  @Max(value = ApplicationFormConstants.APPLICATION_FORM_MAX_REQUEST_COUNT)
  private Integer requestCount; // 요청매수

  @Valid
  @Size(max = 10, message = "희망 구역은 최대 10개까지 등록 가능합니다.")
  private List<HopeAreaRequest> hopeAreaList; // 희망구역 리스트

  private String requestDetails; // 요청사항
}
