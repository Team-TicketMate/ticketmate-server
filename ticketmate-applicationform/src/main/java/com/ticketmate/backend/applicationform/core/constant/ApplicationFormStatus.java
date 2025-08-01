package com.ticketmate.backend.applicationform.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationFormStatus {

  PENDING("대기"),

  APPROVED("승인"),

  CANCELED("취소"),

  REJECTED("거절"),

  CANCELED_IN_PROCESS("진행 취소");

  private final String description;
}
