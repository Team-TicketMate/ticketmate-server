package com.ticketmate.backend.review.core.constant;

import com.ticketmate.backend.common.core.constant.SortField;
import com.ticketmate.backend.common.core.util.CommonUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ReviewSortField implements SortField {

  CREATED_DATE("createdDate"),

  RATING("rating");

  private final String property;
  
  public static ReviewSortField from(String value) {
    return CommonUtil.stringToSortField(ReviewSortField.class, value);
  }

  @Override
  public String getProperty() {
    return property;
  }
}
