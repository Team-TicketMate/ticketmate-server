package com.ticketmate.backend.applicationform.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationFormRejectedType {
  FEE_NOT_MATCHING_MARKET_PRICE("수고비가 시세에 맞지 않음"),

  RESERVATION_CLOSED("예약 마감"),

  SCHEDULE_UNAVAILABLE("티켓팅 일정이 안됨"),

  OTHER("기타");

  private final String description;
}