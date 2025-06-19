package com.ticketmate.backend.global.constant;

public final class PageableConstants {

  // PageableUtil
  public static final int MAX_PAGE_SIZE = 100;
  public static final int DEFAULT_PAGE_SIZE = 10;
  public static final String DEFAULT_SORT_FIELD = "created_date";
  public static final String DEFAULT_SORT_DIRECTION = "DESC";
  private PageableConstants() {
    throw new UnsupportedOperationException("이 유틸리티 클래스는 인스턴스화할 수 없습니다.");
  }

}
