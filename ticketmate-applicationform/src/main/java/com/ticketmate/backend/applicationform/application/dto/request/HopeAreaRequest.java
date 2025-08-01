package com.ticketmate.backend.applicationform.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class HopeAreaRequest {

  @NotNull(message = "우선순위를 입력하세요. 1 ~ 10 정수")
  @Min(value = 1, message = "순위는 1부터 10 사이의 정수만 입력 가능합니다.")
  @Max(value = 10, message = "순위는 1부터 10 사이의 정수만 입력 가능합니다.")
  private Integer priority; // 순위

  @NotNull(message = "희망 구역을 입력하세요.")
  private String location; // 구역

  @NotNull(message = "희망 가격을 입력하세요.(원 단위)")
  @Min(value = 0, message = "희망 가격은 0원 이상이여야 합니다.")
  private Long price; // 가격
}
