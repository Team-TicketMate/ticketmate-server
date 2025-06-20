package com.ticketmate.backend.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PageableConstants {

  // PageableUtil
  public static final int MAX_PAGE_SIZE = 100;
  public static final int DEFAULT_PAGE_SIZE = 10;
  public static final String DEFAULT_SORT_FIELD = "created_date";
  public static final String DEFAULT_SORT_DIRECTION = "DESC";

}
