package com.ticketmate.backend.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketReservationSite {
  INTERPARK_TICKET("인터파크 티켓"),

  YES24_TICKET("예스24 티켓"),

  TICKET_LINK("티켓 링크"),

  MELON_TICKET("멜론 티켓"),

  COUPANG_PLAY("쿠팡 플레이"),

  ETC("기타");

  private final String description;
}
