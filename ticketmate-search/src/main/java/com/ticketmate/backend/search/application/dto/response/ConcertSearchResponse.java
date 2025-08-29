package com.ticketmate.backend.search.application.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConcertSearchResponse implements SearchResult {

  private UUID concertId; // 공연 PK

  private String concertName; // 공연 제목

  private String concertHallName; // 공연장 이름

  private LocalDateTime ticketPreOpenDate; // 티켓 선예매 오픈일

  private LocalDateTime ticketGeneralOpenDate; // 티켓 일반 예매 오픈일

  private LocalDateTime startDate; // 공연 시작일

  private LocalDateTime endDate; // 공연 종료일

  private String concertThumbnailUrl; // 공연 썸네일 URL

  private Double score; // 최종 점수

  @Override
  public void setScore(Double score) {
    this.score = score;
  }

  @Override
  @JsonIgnore
  public UUID getId() {
    return this.concertId;
  }
}
