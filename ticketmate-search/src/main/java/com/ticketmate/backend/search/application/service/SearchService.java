package com.ticketmate.backend.search.application.service;

import com.ticketmate.backend.search.application.dto.CachedSearchResult;
import com.ticketmate.backend.search.application.dto.IdScorePair;
import com.ticketmate.backend.search.application.dto.request.SearchRequest;
import com.ticketmate.backend.search.application.dto.response.SearchResponse;
import com.ticketmate.backend.search.application.dto.response.SearchResult;
import com.ticketmate.backend.search.core.constant.SearchType;
import com.ticketmate.backend.search.infrastructure.repository.SearchRepositoryCustom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

  private final HybridSearchService hybridSearchService;
  private final SearchRepositoryCustom searchRepositoryCustom;

  /**
   * 사용자 요청을 처리하는 메인 검색 메서드
   */
  public SearchResponse<?> search(SearchRequest request) {
    // 하이브리드 검색 결과 가져오기
    CachedSearchResult results = hybridSearchService.executeHybridSearch(request.getKeyword());
    Pageable pageable = request.toPageable();

    Slice<? extends SearchResult> slice;

    if (request.getType() == SearchType.CONCERT) {
      slice = getPaginatedResults(
          results.concertResults(),
          pageable,
          searchRepositoryCustom::findConcertDetailsByIds
      );
    } else {
      slice = getPaginatedResults(
          results.agentResults(),
          pageable,
          searchRepositoryCustom::findAgentDetailsByIds
      );
    }
    if (pageable.getPageNumber() == 0) {
      return SearchResponse.firstPage(slice, results.concertResults().size(), results.agentResults().size());
    }
    return SearchResponse.nextPage(slice);
  }

  /**
   * ID 리스트와 DB 조회 함수를 받아 페이징된 Slice를 반환하는 제네릭 메서드
   *
   * @param allPairList    정렬된 전체 ID 리스트
   * @param pageable       페이지 정보
   * @param detailsFetcher paginated 된 ID 리스트를 받아 실제 DTO 리스트를 반환하는 함수
   * @param <T>            DTO 타입 (e.g., ConcertSearchResponse, AgentSearchResponse)
   * @return 페이징된 DTO가 담긴 Slice 객체
   */
  private <T extends SearchResult> Slice<T> getPaginatedResults(
      List<IdScorePair> allPairList,
      Pageable pageable,
      Function<List<UUID>, List<T>> detailsFetcher) {
    // 먼저 페이징
    List<IdScorePair> paginatedPairList = allPairList.stream()
        .skip(pageable.getOffset())
        .limit(pageable.getPageSize())
        .toList();

    if (paginatedPairList.isEmpty()) {
      return new SliceImpl<>(Collections.emptyList(), pageable, false);
    }

    // 페이징된 id로 dto 조회
    List<T> unorderedDtoList = detailsFetcher.apply(paginatedPairList.stream().map(IdScorePair::id).collect(Collectors.toList()));

    // db 조회 결과 Map으로 변환
    Map<UUID, T> dtoMap = unorderedDtoList.stream()
        .collect(Collectors.toMap(SearchResult::getId, Function.identity()));

    // 정렬 순서대로 최종 리스트 생성 + score 주입
    List<T> results = paginatedPairList.stream()
        .map(pair -> {
          T dto = dtoMap.get(pair.id());
          if (dto != null) {
            dto.setScore(pair.score());
          }
          return dto;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    boolean hasNext = allPairList.size() > pageable.getOffset() + pageable.getPageSize();

    return new SliceImpl<>(results, pageable, hasNext);
  }
}
