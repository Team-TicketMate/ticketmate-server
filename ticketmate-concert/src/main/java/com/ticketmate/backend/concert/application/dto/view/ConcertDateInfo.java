package com.ticketmate.backend.concert.application.dto.view;

import java.time.Instant;

public record ConcertDateInfo(
    Instant performanceDate,
    int session
) {

}
