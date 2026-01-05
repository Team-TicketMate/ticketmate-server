package com.ticketmate.backend.fulfillmentform.core.constant.successhistory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SuccessHistoryStatus {

  NOT_REVIEWED("리뷰가 아직 미작성인 성공내역"),
  REVIEWED("리뷰가 작성된 성공내역");

  private final String description;
}