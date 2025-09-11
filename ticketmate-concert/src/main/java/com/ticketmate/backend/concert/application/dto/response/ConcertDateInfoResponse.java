package com.ticketmate.backend.concert.application.dto.response;

import java.time.LocalDateTime;

public record ConcertDateInfoResponse(
    LocalDateTime performanceDate, // 공연일자
    int session // 회차
) {

}
