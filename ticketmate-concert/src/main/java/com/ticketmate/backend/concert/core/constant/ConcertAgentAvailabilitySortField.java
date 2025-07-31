package com.ticketmate.backend.concert.core.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ticketmate.backend.common.core.constant.SortField;
import com.ticketmate.backend.common.core.util.CommonUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConcertAgentAvailabilitySortField implements SortField {
  // 기본순 (AI 추천)
  TOTAL_SCORE("totalScore"),

  // 별점순 (평균 별점 높은 순)
  AVERAGE_RATING("averageRating"),

  // 후기 많은 순
  REVIEW_COUNT("reviewCount"),

  // 팔로워 많은 순
  FOLLOWER_COUNT("followerCount"),

  // 최근 30일 성공 많은 순
  RECENT_SUCCESS_COUNT("recentSuccessCount");

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
  public static ConcertAgentAvailabilitySortField from(String value) {
    return CommonUtil.stringToEnum(ConcertAgentAvailabilitySortField.class, value);
  }
}
