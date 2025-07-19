package com.ticketmate.backend.domain.vertexai.service;

import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingGeneratorService {
  private final EmbeddingService embeddingService;

  /**
   * 공연 임베딩 데이터를 생성 또는 업데이트
   * 공연 정보 저장/수정 시 호출
   *
   * @param concert 업데이트된 Concert 엔티티
   */
  public void generateOrUpdateConcertEmbedding(Concert concert) {
    ConcertHall concertHall = concert.getConcertHall();

    String embeddingText = String.join(" ",
        concert.getConcertName() != null ? concert.getConcertName() : "",
        concert.getConcertType() != null ? concert.getConcertType().getDescription() : "",
        concertHall != null && concertHall.getConcertHallName() != null ? concertHall.getConcertHallName() : ""
    );

    embeddingService.fetchOrGenerateEmbedding(
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

    if (member == null) {
      log.warn("Portfolio (ID: {})에 연결된 Member가 없어 대리인 임베딩을 생성할 수 없습니다.", portfolio.getPortfolioId());
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    String embeddingText = String.join(" ",
        member.getNickname() != null ? member.getNickname() : "",
        portfolio.getPortfolioDescription() != null ? portfolio.getPortfolioDescription() : ""
    );

    embeddingService.fetchOrGenerateEmbedding(
        member.getMemberId(),
        embeddingText,
        EmbeddingType.AGENT
    );
    log.debug("대리인 임베딩 벡터 생성/업데이트 완료: memberId={}", member.getMemberId());
  }
}
