package com.ticketmate.backend.domain.vertexai.service;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.domain.vertexai.domain.entity.Embedding;
import com.ticketmate.backend.domain.vertexai.repository.EmbeddingRepository;
import com.ticketmate.backend.global.config.EmbeddingConfig;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.util.common.CommonUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {

  private final EmbeddingRepository embeddingRepository;
  private final Client genAiClient;
  private final EmbeddingConfig embeddingConfig;

  /**
   * Vertex AI 임베딩 요청
   * 이미 저장된 임베딩이 있는지 확인
   * 없으면 임베딩 생성 후 저장
   *
   * @param targetId      공연/대리인 ID (검색어의 경우 null)
   * @param text          임베딩 생성할 텍스트 (공연&대리인: plain-text 조합, 검색어: 입력 키워드)
   * @param embeddingType CONCERT, AGENT, SEARCH
   */
  @Transactional
  public Embedding fetchOrGenerateEmbedding(UUID targetId, String text, EmbeddingType embeddingType) {
    // 텍스트 정규화
    String normalizeText = CommonUtil.normalizeAndRemoveSpecialCharacters(text);
    // DB조회 후 반환 or 새로운 임베딩 저장
    return embeddingRepository.findByTextAndEmbeddingType(normalizeText, embeddingType)
        .orElseGet(() -> embeddingRepository.save(
            Embedding.builder()
                .targetId(targetId)
                .text(normalizeText)
                .embeddingVector(extractVector(generateEmbedding(normalizeText)))
                .embeddingType(embeddingType)
                .build())
        );
  }

  /**
   * Vertex AI에 실제로 임베딩 요청을 보내는 로직
   *
   * @param text 임베딩을 생성할 텍스트
   * @return EmbedContentResponse
   */
  private EmbedContentResponse generateEmbedding(String text) {
    try {
      return genAiClient.models.embedContent(
          embeddingConfig.getModel(),
          text,
          EmbedContentConfig.builder().build()
      );
    } catch (ClientException e) {
      log.error("Vertex AI 임베딩 API 호출 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.EMBEDDING_API_ERROR);
    }
  }

  /**
   * 응답에서 벡터 값만 추출해 float[] 로 변환
   *
   * @param response EmbedContentResponse 객체
   * @return 임베딩 float[]
   */
  private float[] extractVector(EmbedContentResponse response) {
    List<Float> embeddingValues = response.embeddings()
        .flatMap(list -> list.stream().findFirst())
        .flatMap(ContentEmbedding::values)
        .orElseThrow(() -> new CustomException(ErrorCode.EMBEDDING_DATA_NOT_FOUND));
    int size = embeddingValues.size();
    float[] vector = new float[size];
    for (int i = 0; i < size; i++) {
      vector[i] = embeddingValues.get(i);
    }
    return vector;
  }

  public List<UUID> findNearestConcerts(float[] queryVector, int limit){
    return embeddingRepository.findNearestConcerts(queryVector, limit);
  }

  public List<UUID> findNearestAgents(float[] queryVector, int limit){
    return embeddingRepository.findNearestAgents(queryVector, limit);
  }
}
