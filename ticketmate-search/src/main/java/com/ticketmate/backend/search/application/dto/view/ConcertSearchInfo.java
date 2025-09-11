package com.ticketmate.backend.search.application.dto.view;

import java.time.Instant;
import java.util.UUID;

public record ConcertSearchInfo(
    UUID concertId, // 공연 PK
    String concertName, // 공연 제목
    String concertHallName, // 공연장 이름
    Instant ticketPreOpenDate, // 티켓 선예매 오픈일
    Instant ticketGeneralOpenDate, // 티켓 일반 예매 오픈일
    Instant startDate, // 공연 시작일
    Instant endDate, // 공연 종료일
    String concertThumbnailStoredPath, // 공연 썸네일 URL
    Double score // 최종 점수
) {

}
