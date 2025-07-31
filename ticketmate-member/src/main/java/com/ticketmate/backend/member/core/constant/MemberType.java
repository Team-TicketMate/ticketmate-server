package com.ticketmate.backend.member.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberType {
  AGENT("대리인"),
  CLIENT("의뢰인");

  private final String description;
}
