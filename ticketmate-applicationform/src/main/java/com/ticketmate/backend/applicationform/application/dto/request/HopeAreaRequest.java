package com.ticketmate.backend.applicationform.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopeAreaRequest {

  @Min(value = 1, message = "순위는 1부터 10 사이의 정수만 입력 가능합니다.")
  @Max(value = 10, message = "순위는 1부터 10 사이의 정수만 입력 가능합니다.")
  private int priority; // 순위

  @NotNull(message = "희망 구역을 입력하세요.")
  private String location; // 구역

  @Positive(message = "가격은 0 이상의 정수만 입력 가능합니다.")
  private int price; // 가격
}
