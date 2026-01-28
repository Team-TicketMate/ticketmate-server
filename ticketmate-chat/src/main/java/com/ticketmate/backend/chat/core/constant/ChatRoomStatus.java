package com.ticketmate.backend.chat.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatRoomStatus {
  ACTIVE("열린 채팅방"),

  CLOSED("닫힌 채팅방");

  private final String description;

}
