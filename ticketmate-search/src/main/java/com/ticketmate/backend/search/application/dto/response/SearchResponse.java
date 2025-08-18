package com.ticketmate.backend.search.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.Collections;

@Getter
public class SearchResponse<T> {

  private final Slice<T> searchResults; // 검색 결과

  @JsonInclude(Include.NON_EMPTY)
  private final Integer concertCount;

  @JsonInclude(Include.NON_EMPTY)
  private final Integer agentCount;

  private SearchResponse(Slice<T> searchResults, Integer concertCount, Integer agentCount) {
    this.searchResults = searchResults;
    this.concertCount = concertCount;
    this.agentCount = agentCount;
  }

  // 첫 페이지용
  public static <T> SearchResponse<T> firstPage(Slice<T> searchResults, int concertCount, int agentCount) {
    return new SearchResponse<>(searchResults, concertCount, agentCount);
  }

  // 다음 페이지용
  public static <T> SearchResponse<T> nextPage(Slice<T> searchResults) {
    return new SearchResponse<>(searchResults, null, null);
  }

  // 빈 결과용
  public static <T> SearchResponse<T> empty() {
    return new SearchResponse<>(new SliceImpl<>(Collections.emptyList()), 0, 0);
  }
}
