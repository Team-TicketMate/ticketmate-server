package com.ticketmate.backend.concert.application.dto.response;

import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import java.time.LocalDateTime;

public record ConcertDateInfoResponse(
    LocalDateTime performanceDate, // 공연일자
    int session // 회차
) {

  public static ConcertDateInfoResponse from(ConcertDate concertDate) {
    return new ConcertDateInfoResponse(concertDate.getPerformanceDate(), concertDate.getSession());
  }
}
