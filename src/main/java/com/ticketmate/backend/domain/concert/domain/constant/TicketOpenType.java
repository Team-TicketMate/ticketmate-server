package com.ticketmate.backend.domain.concert.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketOpenType {

  GENERAL_OPEN("일반예매"),

  PRE_OPEN("선예매"),
  ;

  private final String description;
}
