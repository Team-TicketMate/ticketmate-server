package com.ticketmate.backend.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberType {
  AGENT("대리인"),
  CUSTOMER("구매자");

  private final String description;
}
