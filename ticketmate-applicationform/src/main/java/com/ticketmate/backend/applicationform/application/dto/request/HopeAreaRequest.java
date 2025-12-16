package com.ticketmate.backend.applicationform.application.dto.request;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;

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

  @Min(value = 1, message = "순위는 1부터 5 사이의 정수만 입력 가능합니다.")
  @Max(value = HOPE_AREA_MAX_SIZE, message = "순위는 1부터 5 사이의 정수만 입력 가능합니다.")
  private int priority; // 순위

  @NotBlank(message = "location이 비어있습니다")
  private String location; // 구역

  @Positive(message = "가격은 1 이상의 정수만 입력 가능합니다.")
  private int price; // 가격
}
