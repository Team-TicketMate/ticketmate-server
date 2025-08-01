package com.ticketmate.backend.admin.application.dto.response;

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
public class CoolSmsBalanceResponse {

  private float balance; // 충전된 금액 (원 단위)

  private float point; // 포인트 (금액과 동일)
}
