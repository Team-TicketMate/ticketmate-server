package com.ticketmate.backend.search.application.dto.view;

import java.util.UUID;

public record AgentSearchInfo(
    UUID agentId,
    String nickname,
    String profileImgStoredPath,
    String introduction,
    double averageRating,
    int reviewCount,
    Double score
) {

}
