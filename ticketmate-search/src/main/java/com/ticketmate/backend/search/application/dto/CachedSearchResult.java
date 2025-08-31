package com.ticketmate.backend.search.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CachedSearchResult {
  private List<IdScorePair> concertResults;
  private List<IdScorePair> agentResults;
}
