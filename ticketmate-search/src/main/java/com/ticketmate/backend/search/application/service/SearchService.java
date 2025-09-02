package com.ticketmate.backend.search.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.search.application.dto.CachedSearchResult;
import com.ticketmate.backend.search.application.dto.IdScorePair;
import com.ticketmate.backend.search.application.dto.request.SearchRequest;
import com.ticketmate.backend.search.application.dto.response.SearchResponse;
import com.ticketmate.backend.search.application.dto.response.SearchResult;
import com.ticketmate.backend.search.application.event.SearchEvent;
import com.ticketmate.backend.search.application.mapper.SearchMapper;
import com.ticketmate.backend.search.infrastructure.repository.SearchRepositoryCustom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

  private final HybridSearchService hybridSearchService;
  private final SearchRepositoryCustom searchRepositoryCustom;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final SearchMapper searchMapper;

  /**
   * 사용자 요청을 처리하는 메인 검색 메서드
   */
  public SearchResponse<?> search(SearchRequest request, UUID memberId) {
    String keyword = request.getKeyword();

    if (CommonUtil.nvl(keyword, "").isEmpty()) {
      return SearchResponse.empty();
    }
    // 양옆 공백 제거
    keyword = keyword.trim();

    // 최근 검색어 저장 (로그인 사용자)
    if (memberId != null) {
      applicationEventPublisher.publishEvent(new SearchEvent(memberId, keyword));
    }

    // 하이브리드 검색 결과 가져오기
    CachedSearchResult results = hybridSearchService.executeHybridSearch(keyword);
    Pageable pageable = request.toPageable();

    Slice<? extends SearchResult> slice;

    switch (request.getSearchType()) {
      case CONCERT -> slice = getPaginatedResults(
          results.getConcertResults(),
          pageable,
          searchRepositoryCustom::findConcertDetailsByIds,
          searchMapper::toConcertSearchResponse
      );
      case AGENT -> slice = getPaginatedResults(
          results.getAgentResults(),
          pageable,
          searchRepositoryCustom::findAgentDetailsByIds,
          searchMapper::toAgentSearchResponse
      );
      default -> {
        log.error("잘못된 검색 타입이 요청되었습니다. 요청된 Type: {}", request.getSearchType());
        throw new CustomException(ErrorCode.INVALID_SEARCH_TYPE);
      }
    }

    if (pageable.getPageNumber() == 0) {
      return SearchResponse.firstPage(slice, results.getConcertResults().size(), results.getAgentResults().size());
    }
    return SearchResponse.nextPage(slice);
  }

  /**
   * ID 리스트와 DB 조회 함수를 받아 페이징된 Slice를 반환하는 제네릭 메서드
   *
   * @param allPairList    정렬된 전체 ID 리스트
   * @param pageable       페이지 정보
   * @param detailsFetcher paginated 된 ID 리스트를 받아 실제 DTO 리스트를 반환하는 함수
   * @param mapper         S -> T (ex. ConcertSearchInfo
   * @param <S>            repository에서 조회하는 Info 타입
   * @param <T>            클라이언트 반환 DTO 타입 (e.g., ConcertSearchResponse, AgentSearchResponse)
   * @return 페이징된 DTO가 담긴 Slice 객체
   */
  private <S, T extends SearchResult> Slice<T> getPaginatedResults(
      List<IdScorePair> allPairList,
      Pageable pageable,
      Function<List<UUID>, List<S>> detailsFetcher,
      Function<S, T> mapper
  ) {
    // 먼저 페이징
    List<IdScorePair> paginatedPairList = allPairList.stream()
        .skip(pageable.getOffset())
        .limit(pageable.getPageSize())
        .toList();

    if (paginatedPairList.isEmpty()) {
      return new SliceImpl<>(Collections.emptyList(), pageable, false);
    }

    // 페이징된 id로 dto 조회
    List<S> infoList = detailsFetcher.apply(paginatedPairList.stream()
        .map(IdScorePair::getId)
        .collect(Collectors.toList())
    );

    List<T> unorderedDtoList = infoList.stream()
        .map(mapper)
        .collect(Collectors.toList());

    // db 조회 결과 Map으로 변환
    Map<UUID, T> dtoMap = unorderedDtoList.stream()
        .collect(Collectors.toMap(SearchResult::getId, Function.identity()));

    // 정렬 순서대로 최종 리스트 생성 + score 주입
    List<T> results = paginatedPairList.stream()
        .map(pair -> {
          T dto = dtoMap.get(pair.getId());
          if (dto != null) {
            dto.setScore(pair.getScore());
          }
          return dto;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    boolean hasNext = allPairList.size() > pageable.getOffset() + pageable.getPageSize();

    return new SliceImpl<>(results, pageable, hasNext);
  }
}
