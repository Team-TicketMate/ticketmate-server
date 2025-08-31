package com.ticketmate.backend.search.application.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<IdScorePair> concertResults = List.of();

  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<IdScorePair> agentResults = List.of();
}
