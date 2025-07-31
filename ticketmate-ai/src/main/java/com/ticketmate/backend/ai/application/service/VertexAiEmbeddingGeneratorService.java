package com.ticketmate.backend.ai.application.service;

import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VertexAiEmbeddingGeneratorService {
  private final VertexAiEmbeddingService vertexAiEmbeddingService;

  /**
   * 공연 임베딩 데이터를 생성 또는 업데이트
   * 공연 정보 저장/수정 시 호출
   *
   * @param concert 업데이트된 Concert 엔티티
   */
  public void generateOrUpdateConcertEmbedding(Concert concert) {
    ConcertHall concertHall = concert.getConcertHall();

    String embeddingText = buildEmbeddingText(
        concert.getConcertName(),
        concert.getConcertType() != null ? concert.getConcertType().getDescription() : null,
        concertHall != null ? concertHall.getConcertHallName() : null
    );

    if(embeddingText.trim().isEmpty()){
      log.error("Concert (ID: {})에 대해 임베딩 생성을 위한 유효한 텍스트가 없습니다.", concert.getConcertId());
      throw new CustomException(ErrorCode.INSUFFICIENT_DATA_FOR_EMBEDDING);
    }

    vertexAiEmbeddingService.regenerateEmbedding(
        concert.getConcertId(),
        embeddingText,
        EmbeddingType.CONCERT
    );
    log.debug("공연 임베딩 벡터 생성/업데이트 완료: concertId={}", concert.getConcertId());
  }

  /**
   * 대리인 임베딩 데이터를 생성 또는 업데이트
   * 대리인(MemberType=AGENT)으로 승격될 때 호출
   *
   * @param portfolio 해당 대리인의 Portfolio 엔티티
   */
  public void generateOrUpdateAgentEmbedding(Portfolio portfolio) {
    Member member = portfolio.getMember();

    String embeddingText = buildEmbeddingText(
        member.getNickname(),
        portfolio.getPortfolioDescription()
    );

    if(embeddingText.trim().isEmpty()){
      log.error("Agent (ID: {})에 대해 임베딩 생성을 위한 유효한 텍스트가 없습니다.", member.getMemberId());
      throw new CustomException(ErrorCode.INSUFFICIENT_DATA_FOR_EMBEDDING);
    }

    vertexAiEmbeddingService.regenerateEmbedding(
        member.getMemberId(),
        embeddingText,
        EmbeddingType.AGENT
    );
    log.debug("대리인 임베딩 벡터 생성/업데이트 완료: memberId={}", member.getMemberId());
  }

  /**
   * 임베딩 텍스트 문자열 결합
   *
   * @param texts 연결할 텍스트 배열 (가변 인자)
   * @return 공백으로 연결된 텍스트
   */
  private String buildEmbeddingText(String... texts){
    return Arrays.stream(texts)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }
}
