package com.ticketmate.backend.portfolio.core.constant;

import com.ticketmate.backend.common.core.constant.SortField;
import com.ticketmate.backend.common.core.util.CommonUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PortfolioSortField implements SortField {

  CREATED_DATE("createdDate");

  private final String property;

  /**
   * Jackson이 JSON -> Java 객체로 역직렬화 (deserialization)할 때 자동 호출
   * 컨트롤러에서 들어온 {"sortField": "TICKET_OPEN_DATE"} 같은 문자열을 변환
   * 만약 {"sortField": "ticketOpenDate"}와 같이 카멜케이스로 들어와도 f.property와 비교하여 자동 매칭
   */
  public static PortfolioSortField from(String value) {
    return CommonUtil.stringToSortField(PortfolioSortField.class, value);
  }

  @Override
  public String getProperty() {
    return property;
  }
}
