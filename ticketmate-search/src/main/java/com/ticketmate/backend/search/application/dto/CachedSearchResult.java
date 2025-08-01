package com.ticketmate.backend.search.application.dto;

import java.util.List;

public record CachedSearchResult(
    List<IdScorePair> concertResults,
    List<IdScorePair> agentResults
) {}
