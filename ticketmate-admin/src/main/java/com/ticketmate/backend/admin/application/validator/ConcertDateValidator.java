package com.ticketmate.backend.admin.application.validator;

import com.ticketmate.backend.admin.application.dto.request.ConcertDateRequest;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcertDateValidator {

  private final List<ConcertDateRequest> requestList;

  private ConcertDateValidator(List<ConcertDateRequest> requestList) {
    if (CommonUtil.nullOrEmpty(requestList)) {
      throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
    }
    this.requestList = requestList;
  }

  public static ConcertDateValidator of(List<ConcertDateRequest> requestList) {
    return new ConcertDateValidator(requestList);
  }

  /**
   * 공연 회차 (session)의 최솟값 검증
   */
  public ConcertDateValidator sessionStartsAtOne() {
    int min = requestList.stream()
        .mapToInt(ConcertDateRequest::getSession)
        .min()
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CONCERT_DATE));

    if (min != 1) {
      log.error("첫 공연 회차는 1이어야 합니다. 현재 최솟값: {}", min);
      throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
    }
    return this;
  }

  /**
   * 공연 회차 (session)이 연속적으로 증가하는지 검증
   */
  public ConcertDateValidator sessionContinuity() {
    List<Integer> sessionList = requestList.stream()
        .map(ConcertDateRequest::getSession)
        .sorted()
        .toList();

    for (int i = 0; i < sessionList.size() - 1; i++) {
      if (sessionList.get(i + 1) - sessionList.get(i) != 1) {
        log.error("공연 회차는 연속적으로 증가해야 합니다. 누락된 회차 or 중복된 회차가 존재합니다: {}회차 다음에 {}회차 입력됨",
            sessionList.get(i), sessionList.get(i + 1));
        throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
      }
    }
    return this;
  }

  /**
   * 날짜와 회차가 올바르게 매칭되었는지 검증
   */
  public ConcertDateValidator dateSessionOrder() {
    List<ConcertDateRequest> sortedByDate = requestList.stream()
        .sorted(Comparator.comparing(ConcertDateRequest::getPerformanceDate))
        .toList();

    for (int i = 0; i < sortedByDate.size() - 1; i++) {
      LocalDateTime prevPerformanceDate = sortedByDate.get(i).getPerformanceDate();
      LocalDateTime nextPerformanceDate = sortedByDate.get(i + 1).getPerformanceDate();
      int prevSession = sortedByDate.get(i).getSession();
      int nextSession = sortedByDate.get(i + 1).getSession();

      // 날짜는 빠른데 회차가 더 늦는 경우
      if (prevSession >= nextSession) {
        log.error("공연 날짜와 회차의 순서가 일치하지 않습니다. 빠른 날짜({})의 회차({})가 늦은 날짜({})의 회차({})보다 큽니다.",
            prevPerformanceDate, prevSession, nextPerformanceDate, nextSession);
        throw new CustomException(ErrorCode.INVALID_CONCERT_DATE);
      }
    }
    return this;
  }
}
