package com.ticketmate.backend.member.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatus {

  ACTIVE("활성"),

  WITHDRAWN("탈퇴"),

  TEMP_BAN("일시정지"),

  PERMANENT_BAN("영구정지"),
  ;

  private final String description;
}
