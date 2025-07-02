package com.ticketmate.backend.domain.vertexai.service;

import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.domain.vertexai.domain.entity.Embedding;
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

  @Test
  void 임베딩_저장() {
    log.info("임베딩 저장 시작");
    Embedding embedding = embeddingService.fetchOrGenerateEmbedding(null, "안녕하세요", EmbeddingType.SEARCH);
    log.info("임베딩 저장 성공");
    log.info("임베딩 targetId: {}, text: {}, embeddingVector: {}, embeddingType: {}",
        embedding.getTargetId(), embedding.getText(), embedding.getEmbeddingVector(), embedding.getEmbeddingType());
  }
}