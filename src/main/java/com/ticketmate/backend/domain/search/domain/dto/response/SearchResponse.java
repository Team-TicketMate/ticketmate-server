package com.ticketmate.backend.domain.search.domain.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
public class SearchResponse<T> {
  private final Slice<T> searchResults; // 검색 결과
  private final Integer concertCount;
  private final Integer agentCount;

  private SearchResponse(Slice<T> searchResults, Integer concertCount, Integer agentCount){
    this.searchResults = searchResults;
    this.concertCount = concertCount;
    this.agentCount = agentCount;
  }

  // 첫 페이지용
  public static <T> SearchResponse<T> firstPage(Slice<T> searchResults, int concertCount, int agentCount){
    return new SearchResponse<>(searchResults, concertCount, agentCount);
  }

  // 다음 페이지용
  public static <T> SearchResponse<T> nextPage(Slice<T> searchResults){
    return new SearchResponse<>(searchResults, null, null);
  }
}
