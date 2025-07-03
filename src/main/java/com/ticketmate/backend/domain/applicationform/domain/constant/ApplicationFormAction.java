package com.ticketmate.backend.domain.applicationform.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationFormAction {

  EDIT("수정"),

  CANCEL("취소"),

  CANCEL_IN_PROCESS("진행취소"),

  REJECT("거절"),

  ACCEPT("수락");

  private final String description;
}
