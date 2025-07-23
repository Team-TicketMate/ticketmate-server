package com.ticketmate.backend.domain.search.domain.dto;

import java.util.List;

public record CachedSearchResult(
    List<IdScorePair> concertResults,
    List<IdScorePair> agentResults
) {}
