package com.ticketmate.backend.chat.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum
ChatMessageType {
  TEXT("텍스트"),

  PICTURE("이미지"),

  FULFILLMENT_FORM("성공양식 전송"),

  ACCEPTED_FULFILLMENT_FORM("성공양식 수락"),

  REJECTED_FULFILLMENT_FORM("성공양식 거절"),

  UPDATE_FULFILLMENT_FORM("성공양식 수정");

  private final String description;
}
