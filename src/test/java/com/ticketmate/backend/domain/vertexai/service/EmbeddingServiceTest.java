package com.ticketmate.backend.domain.vertexai.service;

import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.domain.vertexai.domain.entity.Embedding;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class EmbeddingServiceTest {

  @Autowired
  EmbeddingService embeddingService;

  private static final int LIMIT = 100;

  @Test
  void 임베딩_저장() {
    log.info("임베딩 저장 시작");
    Embedding embedding = embeddingService.fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH);
    log.info("임베딩 저장 성공");
    log.info("임베딩 targetId: {}, text: {}, embeddingVector: {}, embeddingType: {}",
        embedding.getTargetId(), embedding.getText(), embedding.getEmbeddingVector(), embedding.getEmbeddingType());
  }

  @Test
  void 벡터_검색_콘서트_시간측정(){
    float[] queryVector = embeddingService
        .fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH)
        .getEmbeddingVector();

    // 워밍업
    embeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.CONCERT);

    long start = System.currentTimeMillis();
    List<UUID> concertIds = embeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.CONCERT);
    long duration = System.currentTimeMillis() - start;

    log.info("findNearestConcerts 벡터 검색 시간: {}ms, 결과 개수: {}",
        duration, concertIds.size());
  }

  @Test
  void 벡터_검색_에이전트_시간측정() {
    float[] queryVector = embeddingService
        .fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH)
        .getEmbeddingVector();

    // 워밍업
    embeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.AGENT);

    long start = System.currentTimeMillis();
    List<UUID> agentIds = embeddingService.findNearestEmbeddings(queryVector, LIMIT, EmbeddingType.AGENT);
    long duration = System.currentTimeMillis() - start;

    log.info("findNearestAgents 벡터 검색 시간: {}ms, 결과 개수: {}",
        duration, agentIds.size());
  }
}