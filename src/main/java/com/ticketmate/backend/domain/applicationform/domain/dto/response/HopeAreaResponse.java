package com.ticketmate.backend.domain.applicationform.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HopeAreaResponse {

  private Integer priority; // 우선 순위 (1~10)

  private String location; // 위치 (예: A구역, B구역)

  private Long price; // 가격
}
