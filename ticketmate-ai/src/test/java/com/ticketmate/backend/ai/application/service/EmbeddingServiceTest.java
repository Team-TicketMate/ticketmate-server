//package com.ticketmate.backend.ai.application.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.ticketmate.backend.ai.core.constant.EmbeddingType;
//import com.ticketmate.backend.ai.infrastructure.entity.Embedding;
//import com.ticketmate.backend.ai.infrastructure.repository.EmbeddingRepository;
//import com.ticketmate.backend.common.core.util.CommonUtil;
//import java.util.List;
//import java.util.UUID;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.cache.Cache;
//import org.springframework.cache.CacheManager;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@ActiveProfiles("dev")
//@Slf4j
//class EmbeddingServiceTest {
//
//  @Autowired
//  VertexAiEmbeddingService vertexAiEmbeddingService;
//
//  @Autowired
//  EmbeddingRepository embeddingRepository;
//
//  @Autowired
//  private CacheManager cacheManager;
//
//  private static final int LIMIT = 100;
//
//  @Test
//  void 임베딩_저장() {
//    log.info("임베딩 저장 시작");
//    Embedding embedding = vertexAiEmbeddingService.fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH);
//    log.info("임베딩 저장 성공");
//    log.info("임베딩 targetId: {}, text: {}, embeddingVector: {}, embeddingType: {}",
//        embedding.getTargetId(), embedding.getText(), embedding.getEmbeddingVector(), embedding.getEmbeddingType());
//  }
//
//  @Test
//  void 벡터_검색_콘서트_시간측정(){
//    float[] queryVector = vertexAiEmbeddingService
//        .fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH)
//        .getEmbeddingVector();
//
//    // 워밍업
//    vertexAiEmbeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.CONCERT);
//
//    long start = System.currentTimeMillis();
//    List<UUID> concertIds = vertexAiEmbeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.CONCERT);
//    long duration = System.currentTimeMillis() - start;
//
//    log.info("findNearestConcerts 벡터 검색 시간: {}ms, 결과 개수: {}",
//        duration, concertIds.size());
//  }
//
//  @Test
//  void 벡터_검색_에이전트_시간측정() {
//    float[] queryVector = vertexAiEmbeddingService
//        .fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH)
//        .getEmbeddingVector();
//
//    // 워밍업
//    vertexAiEmbeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.AGENT);
//
//    long start = System.currentTimeMillis();
//    List<UUID> agentIds = vertexAiEmbeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.AGENT);
//    long duration = System.currentTimeMillis() - start;
//
//    log.info("findNearestAgents 벡터 검색 시간: {}ms, 결과 개수: {}",
//        duration, agentIds.size());
//  }
//
//  @Test
//  @Transactional // 롤백 보장
//  void 임베딩_조회_캐싱_성능측정() {
//    // 검색 키워드
//    String keyword = "대구";
//
//    // 캐시 삭제
//    String cacheKey = CommonUtil.normalizeAndRemoveSpecialCharacters(keyword) + ":" + EmbeddingType.SEARCH;
//    Cache embeddingsCache = cacheManager.getCache("embeddings");
//    embeddingsCache.evict(cacheKey); // 전체 clear() 대신 특정 키만 evict()
//    log.info("--- 사전 준비: 캐시에서 '{}' 키 삭제 완료 ---", cacheKey);
//
//    // DB 삭제
//    embeddingRepository.deleteByText(keyword);
//    log.info("--- 사전 준비: '{}' 키워드 임베딩 데이터 DB 삭제 완료 ---", keyword);
//
//    // Cache Miss, DB Miss
//    log.info("--- 시나리오 1: 최초 호출 (Cache Miss, DB Miss) ---");
//    long start1 = System.currentTimeMillis();
//    vertexAiEmbeddingService.fetchOrGenerateEmbedding(null, keyword, EmbeddingType.SEARCH);
//    long duration1 = System.currentTimeMillis() - start1;
//    log.info("fetchOrGenerateEmbedding 호출 시간 (API 호출 + DB 저장): {}ms", duration1);
//
//    // Cache Miss, DB Hit
//    cacheManager.getCache("embeddings").clear();
//    log.info("--- 시나리오 2: DB 조회 (Cache Miss, DB Hit) ---");
//    long start2 = System.currentTimeMillis();
//    vertexAiEmbeddingService.fetchOrGenerateEmbedding(null, keyword, EmbeddingType.SEARCH);
//    long duration2 = System.currentTimeMillis() - start2;
//    log.info("fetchOrGenerateEmbedding 호출 시간 (DB 조회): {}ms", duration2);
//
//    // Cache Hit
//    log.info("--- 시나리오 3: 캐시 조회 시나리오 실행 (Cache Hit) ---");
//    long start3 = System.currentTimeMillis();
//    vertexAiEmbeddingService.fetchOrGenerateEmbedding(null, keyword, EmbeddingType.SEARCH);
//    long duration3 = System.currentTimeMillis() - start3;
//    log.info("fetchOrGenerateEmbedding 호출 시간 (캐시 조회): {}ms", duration3);
//
//    // 성능비교
//    // THEN: 성능 비교
//    assertThat(duration3).isLessThan(duration2);
//    assertThat(duration2).isLessThan(duration1);
//    log.info("성능 최종 비교: 캐시({}ms) < DB({}ms) < API({}ms)", duration3, duration2, duration1);
//  }
//}