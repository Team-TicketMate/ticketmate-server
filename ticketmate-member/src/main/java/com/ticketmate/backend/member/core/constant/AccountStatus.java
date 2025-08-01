package com.ticketmate.backend.member.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountStatus {
  ACTIVE_ACCOUNT("활성화된 계정"),
  DELETE_ACCOUNT("삭제된 계정");

  private final String description;
}
