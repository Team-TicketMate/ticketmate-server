package com.ticketmate.backend.search.application.service;

import com.ticketmate.backend.ai.application.service.VertexAiEmbeddingService;
import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.ai.infrastructure.entity.Embedding;
import com.ticketmate.backend.search.application.dto.CachedSearchResult;
import com.ticketmate.backend.search.application.dto.IdScorePair;
import com.ticketmate.backend.search.infrastructure.repository.SearchRepositoryCustom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HybridSearchService {
  private final VertexAiEmbeddingService vertexAiEmbeddingService;
  private final SearchRepositoryCustom searchRepositoryCustom;
  @Qualifier("applicationTaskExecutor")
  private final TaskExecutor taskExecutor;

  private static final int LIMIT = 100;
  private static final int RRF_K = 60;


  /**
   * 실제 검색 로직을 수행하고 Redis에 결과를 캐싱하는 메서드
   * TTL 30분 설정
   * 데이터 변경(공연 저장/수정, 대리인 승격 등) 시 AdminService, PortfolioCacheEvictListener에서 해당 캐시 무효화
   */
  @Cacheable(value = "searches", key = "#keyword")
  public CachedSearchResult executeHybridSearch(String keyword){
    Embedding queryEmbedding = vertexAiEmbeddingService.fetchOrGenerateEmbedding(null, keyword, EmbeddingType.SEARCH);
    float[] queryVector = queryEmbedding.getEmbeddingVector();

    // 공연 및 대리인 결과 병렬 조회
    CompletableFuture<List<IdScorePair>> concertFuture =
        CompletableFuture.supplyAsync(
            () -> getRankedConcertResults(keyword, queryVector),
            taskExecutor
        );
    CompletableFuture<List<IdScorePair>> agentFuture =
        CompletableFuture.supplyAsync(
            () -> getRankedAgentResults(keyword, queryVector),
            taskExecutor
        );

    // 모두 완료될 때까지 대기
    CompletableFuture.allOf(concertFuture, agentFuture).join();

    List<IdScorePair> concertList = concertFuture.join();
    List<IdScorePair> agentList = agentFuture.join();

    return new CachedSearchResult(concertList, agentList);
  }

  /**
   * 두 종류의 ID 조회 함수를 받아 RRF 점수를 계산하고 정렬된 IdScorePair 리스트를 반환하는 공통 메서드
   *
   * @param vectorIdFetcher 벡터 검색 ID 조회 함수
   * @param keywordIdFetcher 키워드 검색 ID 조회 함수
   * @return 정렬된 IdScorePair 리스트
   */
  private List<IdScorePair> getRankedResults(
      Supplier<List<UUID>> vectorIdFetcher,
      Supplier<List<UUID>> keywordIdFetcher
  ){
    // 벡터 및 키워드 기반 id 리스트 조회
    List<UUID> vectorIdList = vectorIdFetcher.get();
    List<UUID> keywordIdList = keywordIdFetcher.get();

    // RRF 점수 계산
    Map<UUID, Double> scoreMap = calculateScores(vectorIdList, keywordIdList);

    return getRankedIdScorePairs(scoreMap);
  }

  private List<IdScorePair> getRankedIdScorePairs(Map<UUID, Double> scoreMap){
    return scoreMap.entrySet().stream()
        .sorted(Entry.<UUID, Double>comparingByValue().reversed())
        .map(entry -> new IdScorePair(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  private List<IdScorePair> getRankedConcertResults(String keyword, float[] queryVector){
    return getRankedResults(
        () -> vertexAiEmbeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.CONCERT),
        () -> searchRepositoryCustom.findConcertIdsByKeyword(keyword, LIMIT)
    );
  }

  private List<IdScorePair> getRankedAgentResults(String keyword, float[] queryVector){
    return getRankedResults(
        () -> vertexAiEmbeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.AGENT),
        () -> searchRepositoryCustom.findAgentIdsByKeyword(keyword, LIMIT)
    );
  }

  private Map<UUID, Double> calculateScores(List<UUID> vectorIdList, List<UUID> likeIdList){
    Map<UUID, Double> scoreMap = new HashMap<>();
    // 벡터 검색 결과에 대한 RRF 점수 계산
    for (int i = 0; i < vectorIdList.size(); i++) {
      UUID id = vectorIdList.get(i);
      double rrfScore = 1.0 / (RRF_K + (i + 1));
      scoreMap.put(id, rrfScore);
    }

    // 키워드 검색 결과에 대한 RRF 점수 계산 및 병합
    for (int i = 0; i < likeIdList.size(); i++) {
      UUID id = likeIdList.get(i);
      double rrfScore = 1.0 / (RRF_K + (i + 1));
      // 기존 점수(벡터 검색)에 현재 점수(키워드 검색)를 합산
      scoreMap.merge(id, rrfScore, Double::sum);
    }

    return scoreMap;
  }
}
