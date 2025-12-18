package com.ticketmate.backend.ai.application.service;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.ai.infrastructure.entity.Embedding;
import com.ticketmate.backend.ai.infrastructure.properties.GoogleGenAiProperties;
import com.ticketmate.backend.ai.infrastructure.repository.EmbeddingRepository;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VertexAiEmbeddingService {

  private final EmbeddingRepository embeddingRepository;
  private final Client genAiClient;
  private final GoogleGenAiProperties googleGenAIProperties;

  /**
   * Vertex AI 임베딩 요청
   * 검색용 임베딩 생성/조회
   * TTL 없이 영구 캐시 적용
   *
   * @param targetId      공연/대리인 ID (검색어의 경우 null)
   * @param text          임베딩 생성할 텍스트 (공연&대리인: plain-text 조합, 검색어: 입력 키워드)
   * @param embeddingType CONCERT, AGENT, SEARCH
   */
  @Transactional
  @Caching(
      cacheable = @Cacheable(
          value = "embeddings",
          key = "T(com.ticketmate.backend.common.core.util.CommonUtil)"
                + ".normalizeAndRemoveSpecialCharacters(#text)"
                + "+':' + #embeddingType",
          condition = "#targetId == null",
          unless = "#result == null"
      )
  )
  public Embedding fetchOrGenerateEmbedding(UUID targetId, String text, EmbeddingType embeddingType) {
    // 텍스트 정규화
    String normalizeText = CommonUtil.normalizeAndRemoveSpecialCharacters(text);

    // 검색 모드: 캐시나 DB 조회 후 없으면 생성
    return embeddingRepository.findByTextAndEmbeddingType(normalizeText, embeddingType)
        .orElseGet(() -> createAndSaveEmbedding(targetId, normalizeText, embeddingType));
  }

  /**
   * 실제 Embedding 생성 및 저장 로직
   */
  private Embedding createAndSaveEmbedding(UUID targetId, String normalizedText, EmbeddingType type) {
    // Vertex AI 호출하여 벡터 생성
    float[] vector = extractVector(generateEmbedding(normalizedText));

    return embeddingRepository.save(Embedding.builder()
        .targetId(targetId)
        .text(normalizedText)
        .embeddingVector(vector)
        .embeddingType(type)
        .build());
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
          googleGenAIProperties.model(),
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

  /**
   * 벡터 탐색 결과 반환 (공연)
   * 지정된 queryVector로 유사도가 높은 순서대로 정렬된 리스트 조회
   *
   * @param queryVector   검색에 사용할 임베딩 벡터
   * @param limit         조회할 최대 아이디 개수
   * @return 유사도 순으로 정렬된 대리인 ID 리스트
   */
  @Transactional(readOnly = true)
  public List<UUID> findNearestConcertEmbeddings(float[] queryVector, int limit) {
    return embeddingRepository.findNearestConcertEmbeddings(queryVector, limit);
  }

  /**
   * 벡터 탐색 결과 반환 (대리인)
   * 지정된 queryVector로 유사도가 높은 순서대로 정렬된 리스트 조회
   *
   * @param queryVector   검색에 사용할 임베딩 벡터
   * @param limit         조회할 최대 아이디 개수
   * @return 유사도 순으로 정렬된 대리인 ID 리스트
   */
  @Transactional(readOnly = true)
  public List<UUID> findNearestAgentEmbeddings(float[] queryVector, int limit) {
    return embeddingRepository.findNearestAgentEmbeddings(queryVector, limit);
  }
}
