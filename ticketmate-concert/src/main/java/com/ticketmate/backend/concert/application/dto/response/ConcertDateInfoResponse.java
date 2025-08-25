package com.ticketmate.backend.concert.application.dto.response;

import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertDateInfoResponse {

  private LocalDateTime performanceDate; // 공연일자

  private int session; // 회차

  public static ConcertDateInfoResponse from(ConcertDate concertDate) {
    return ConcertDateInfoResponse.builder()
        .performanceDate(concertDate.getPerformanceDate())
        .session(concertDate.getSession())
        .build();
  }
}
