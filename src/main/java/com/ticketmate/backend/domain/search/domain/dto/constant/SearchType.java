package com.ticketmate.backend.domain.search.domain.dto.constant;

import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchType {
  CONCERT(EmbeddingType.CONCERT),
  AGENT(EmbeddingType.AGENT);

  private final EmbeddingType embeddingType;
}
