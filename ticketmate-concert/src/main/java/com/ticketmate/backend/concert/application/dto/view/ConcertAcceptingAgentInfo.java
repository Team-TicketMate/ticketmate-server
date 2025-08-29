package com.ticketmate.backend.concert.application.dto.view;

import java.util.UUID;

public record ConcertAcceptingAgentInfo(
    UUID agentId,
    String nickname,
    String profileImgStoredPath,
    String introduction,
    double averageRating,
    int reviewCount
) {

}
