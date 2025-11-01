package com.ticketmate.backend.concertagentavailability.application.dto.response;

import java.util.UUID;

public record AgentAcceptingConcertResponse(
    UUID concertId,
    String concertName,
    String concertThumbnailStoredPath,
    int matchedClientCount
) {}