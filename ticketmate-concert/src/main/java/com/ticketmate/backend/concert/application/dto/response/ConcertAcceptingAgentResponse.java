package com.ticketmate.backend.concert.application.dto.response;

import java.util.UUID;

public record ConcertAcceptingAgentResponse(
    UUID agentId,
    String nickname,
    String profileUrl,
    String introduction,
    double averageRating,
    int reviewCount
) {

}
