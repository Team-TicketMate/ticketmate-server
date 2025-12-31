package com.ticketmate.backend.fulfillmentform.core.constant.fulfillmentform;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FulfillmentFormStatus {
  PENDING_FULFILLMENT_FORM("대기중인 성공양식"),
  ACCEPTED_FULFILLMENT_FORM("수락된 성공양식"),
  UPDATE_FULFILLMENT_FORM("수정된 성공양식"),
  REJECTED_FULFILLMENT_FORM("거절된 성공양식");

  private final String description;

}
