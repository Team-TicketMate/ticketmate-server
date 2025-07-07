package com.ticketmate.backend.domain.concert.domain.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ticketmate.backend.global.constant.SortField;
import com.ticketmate.backend.global.util.common.CommonUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConcertSortField implements SortField {
  CREATED_DATE("createdDate"),
  TICKET_OPEN_DATE("ticketOpenDate");

  private final String property;

  @Override
  public String getProperty() {
    return property;
  }

  /**
   * Jackson이 JSON -> Java 객체로 역직렬화 (deserialization)할 때 자동 호출
   * 컨트롤러에서 들어온 {"sortField": "TICKET_OPEN_DATE"} 같은 문자열을 변환
   * 만약 {"sortField": "ticketOpenDate"}와 같이 카멜케이스로 들어와도 f.property와 비교하여 자동 매칭
   */
  @JsonCreator
  public static ConcertSortField from(String value) {
    return CommonUtil.stringToEnum(ConcertSortField.class, value);
  }
}
