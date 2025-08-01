package com.ticketmate.backend.concert.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime performanceDate; // 공연일자
  private Integer session; // 회차

  public static ConcertDateInfoResponse from(ConcertDate concertDate) {
    return ConcertDateInfoResponse.builder()
        .performanceDate(concertDate.getPerformanceDate())
        .session(concertDate.getSession())
        .build();
  }
}
