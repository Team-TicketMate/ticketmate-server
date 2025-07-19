package com.ticketmate.backend.domain.search.domain.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedSearchResult implements Serializable {
  private List<IdScorePair> concertResults;
  private List<IdScorePair> agentResults;
}
