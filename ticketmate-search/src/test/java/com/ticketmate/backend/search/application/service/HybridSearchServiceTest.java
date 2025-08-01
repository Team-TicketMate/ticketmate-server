//package com.ticketmate.backend.search.application.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.ticketmate.backend.ai.core.constant.EmbeddingType;
//import com.ticketmate.backend.ai.core.service.EmbeddingService;
//import com.ticketmate.backend.ai.infrastructure.repository.EmbeddingRepository;
//import com.ticketmate.backend.common.core.util.CommonUtil;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
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
//public class HybridSearchServiceTest {
//  @Autowired
//  private HybridSearchService hybridSearchService;
//
//  @Autowired
//  private EmbeddingService embeddingService;
//
//  @Autowired
//  private EmbeddingRepository embeddingRepository;
//
//  @Autowired
//  private CacheManager cacheManager;
//
//  private final String TEST_KEYWORD = "대구";
//
//  @Test
//  @Transactional
//  void 해시브리드검색_캐시전략별_성능비교(){
//    Cache embeddingsCache = cacheManager.getCache("embeddings");
//    Cache searchesCache = cacheManager.getCache("searches");
//
//    String embeddingCacheKey = CommonUtil.normalizeAndRemoveSpecialCharacters(TEST_KEYWORD) + ":" + EmbeddingType.SEARCH;
//    String searchesCacheKey = TEST_KEYWORD;
//
//    log.info("--- 시나리오 1: 모든 캐시 미적용 (API 호출) ---");
//    // 사전 준비: 캐시 삭제
//    embeddingsCache.evict(embeddingCacheKey);
//    searchesCache.evict(searchesCacheKey);
//    // 사전 준비: DB에서 임베딩 삭제
//    embeddingRepository.deleteByText(CommonUtil.normalizeAndRemoveSpecialCharacters(TEST_KEYWORD));
//
//    long start1 = System.currentTimeMillis();
//    hybridSearchService.executeHybridSearch(TEST_KEYWORD);
//    long duration_baseline = System.currentTimeMillis() - start1;
//    log.info("하이브리드 검색 소요 시간 (Baseline): {}ms", duration_baseline);
//
//
//    log.info("--- 시나리오 2: 'embeddings' 캐시만 적용 (DB 조회) ---");
//    // 사전 준비: embeddings 캐시는 채워진 상태, searches 캐시만 삭제
//    searchesCache.evict(searchesCacheKey);
//
//    long start2 = System.currentTimeMillis();
//    hybridSearchService.executeHybridSearch(TEST_KEYWORD);
//    long duration_embedding_hit = System.currentTimeMillis() - start2;
//    log.info("하이브리드 검색 소요 시간 ('embeddings' Hit): {}ms", duration_embedding_hit);
//
//
//    log.info("--- 시나리오 3: 검색 결과 캐시 조회 ('searches' 캐시 적중) ---");
//    long start3 = System.currentTimeMillis();
//    hybridSearchService.executeHybridSearch(TEST_KEYWORD);
//    long duration_search_hit = System.currentTimeMillis() - start3;
//    log.info("하이브리드 검색 소요 시간  ('searches' Hit): {}ms", duration_search_hit);
//
//    log.info("====================================================================");
//    log.info("최종 성능 비교");
//    log.info(" - Baseline (모두 미적용): \t{}ms", duration_baseline);
//    log.info(" - Embeddings 캐시만 적용: \t{}ms", duration_embedding_hit);
//    log.info(" - Searches 캐시 적용: \t\t{}ms", duration_search_hit);
//    log.info("====================================================================");
//
//    assertThat(duration_search_hit).isLessThan(duration_embedding_hit);
//    assertThat(duration_embedding_hit).isLessThan(duration_baseline);
//  }
//
//  @Test
//  void 하이브리드검색_동시요청_성능측정() throws InterruptedException {
//    int numberOfThreads = 10; // 동시 요청 스레드 수
//    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//    Cache searchesCache = cacheManager.getCache("searches");
//    String searchesCacheKey = TEST_KEYWORD;
//
//    // 사전 준비: 단일 API 호출로 캐시 및 DB 데이터 생성
//    log.info("--- [동시] 사전 준비: 단일 API 호출 실행으로 데이터 생성 ---");
//    hybridSearchService.executeHybridSearch(TEST_KEYWORD);
//
//    log.info("--- [동시] 시나리오 1: Embeddings 캐시만 적용된 상태에서 동시 요청 ---");
//    // 사전 준비: searches 캐시 제거
//    searchesCache.evict(searchesCacheKey);
//    long start1 = System.currentTimeMillis();
//    executeConcurrentSearch(executorService, numberOfThreads);
//    long duration_concurrent_db = System.currentTimeMillis() - start1;
//    log.info("[동시] 전체 소요 시간 (DB 조회 경쟁): {}ms", duration_concurrent_db);
//
//    log.info("--- [동시] 시나리오 2: 모든 캐시가 적용된 상태에서 동시 요청 ---");
//    long start2 = System.currentTimeMillis();
//    executeConcurrentSearch(executorService, numberOfThreads);
//    long duration_concurrent_cache = System.currentTimeMillis() - start2;
//    log.info("[동시] 전체 소요 시간 (캐시 조회 경쟁): {}ms", duration_concurrent_cache);
//
//
//    log.info("====================================================================");
//    log.info("최종 성능 비교 (동시 요청 환경)");
//    log.info(" - 시나리오 2 (DB 조회 경쟁): \t{}ms", duration_concurrent_db);
//    log.info(" - 시나리오 3 (캐시 조회 경쟁): \t{}ms", duration_concurrent_cache);
//    log.info("====================================================================");
//
//    executorService.shutdown();
//  }
//
//  private void executeConcurrentSearch(ExecutorService executorService, int numberOfThreads) throws InterruptedException {
//    CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
//    for (int i = 0; i < numberOfThreads; i++) {
//      executorService.submit(() -> {
//        try {
//          hybridSearchService.executeHybridSearch(TEST_KEYWORD);
//        } finally {
//          doneLatch.countDown();
//        }
//      });
//    }
//    // 모든 스레드가 작업을 마칠 때까지 대기
//    doneLatch.await(30, TimeUnit.SECONDS);
//  }
//}
