package com.ticketmate.backend.domain.search.domain.dto.response;

import java.util.UUID;

public interface SearchResult {
  void setScore(Double score);
  UUID getId();
}
