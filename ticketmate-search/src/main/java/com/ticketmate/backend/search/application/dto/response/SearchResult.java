package com.ticketmate.backend.search.application.dto.response;

import java.util.UUID;

public interface SearchResult {
  void setScore(Double score);
  UUID getId();
}
