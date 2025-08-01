package com.ticketmate.backend.search.core.constant;

import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchType {
  CONCERT(EmbeddingType.CONCERT),
  AGENT(EmbeddingType.AGENT);

  private final EmbeddingType embeddingType;
}
