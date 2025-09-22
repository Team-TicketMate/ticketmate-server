package com.ticketmate.backend.search.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.QConcert;
import com.ticketmate.backend.concert.infrastructure.entity.QConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.QTicketOpenDate;
import com.ticketmate.backend.concerthall.infrastructure.entity.QConcertHall;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.QAgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.portfolio.infrastructure.entity.QPortfolio;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import com.ticketmate.backend.search.application.dto.view.AgentSearchInfo;
import com.ticketmate.backend.search.application.dto.view.ConcertSearchInfo;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepositoryCustom {

  private static final QMember MEMBER = QMember.member;
  private static final QAgentPerformanceSummary AGENT_PERFORMANCE_SUMMARY = QAgentPerformanceSummary.agentPerformanceSummary;
  private static final QConcert CONCERT = QConcert.concert;
  private static final QConcertDate CONCERT_DATE = QConcertDate.concertDate;
  private static final QTicketOpenDate TICKET_OPEN_DATE = QTicketOpenDate.ticketOpenDate;
  private static final QConcertHall CONCERT_HALL = QConcertHall.concertHall;
  private static final QPortfolio PORTFOLIO = QPortfolio.portfolio;

  private final JPAQueryFactory queryFactory;

  /**
   * 제공된 ID 목록에 해당하는 공연들의 상세 정보를 조회하여 ConcertSearchInfo DTO 리스트로 반환합니다.
   *
   * @param concertIds 상세 정보를 조회할 공연의 UUID 리스트
   * @return 공연 상세 정보가 담긴 DTO 리스트
   */
  @Override
  public List<ConcertSearchInfo> findConcertDetailsByIds(List<UUID> concertIds) {
    if (concertIds == null || concertIds.isEmpty()) {
      return Collections.emptyList();
    }
    return queryFactory
        .select(Projections.constructor(ConcertSearchInfo.class,
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            // 선예매 오픈일
            Expressions.dateTimeTemplate(
                Instant.class,
                "min({0})",
                new CaseBuilder()
                    .when(TICKET_OPEN_DATE.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
                    .then(TICKET_OPEN_DATE.openDate)
                    .otherwise((Instant) null)
            ).as("ticketPreOpenDate"),
            // 일반 예매 오픈일
            Expressions.dateTimeTemplate(
                Instant.class,
                "min({0})",
                new CaseBuilder()
                    .when(TICKET_OPEN_DATE.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
                    .then(TICKET_OPEN_DATE.openDate)
                    .otherwise((Instant) null)
            ).as("ticketGeneralOpenDate"),
            CONCERT_DATE.performanceDate.min().as("startDate"),
            CONCERT_DATE.performanceDate.max().as("endDate"),
            CONCERT.concertThumbnailStoredPath,
            Expressions.constant(0.0)
        ))
        .from(CONCERT)
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        .join(CONCERT_DATE).on(CONCERT.eq(CONCERT_DATE.concert))
        .join(TICKET_OPEN_DATE).on(CONCERT.eq(TICKET_OPEN_DATE.concert))
        .where(CONCERT.concertId.in(concertIds))
        .groupBy(CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertThumbnailStoredPath
        )
        .fetch();
  }

  /**
   * 제공된 ID 목록에 해당하는 대리인의 상세 정보를 조회하여 AgentSearchInfo DTO 리스트로 반환합니다.
   *
   * @param agentIds 상세 정보를 조회할 대리인의 UUID 리스트
   * @return 대리인 상세 정보가 담긴 DTO 리스트
   */
  @Override
  public List<AgentSearchInfo> findAgentDetailsByIds(List<UUID> agentIds) {
    if (agentIds == null || agentIds.isEmpty()) {
      return Collections.emptyList();
    }
    return queryFactory
        .select(Projections.constructor(AgentSearchInfo.class,
            MEMBER.memberId,
            MEMBER.nickname,
            MEMBER.profileImgStoredPath,
            PORTFOLIO.portfolioDescription,
            AGENT_PERFORMANCE_SUMMARY.averageRating,
            AGENT_PERFORMANCE_SUMMARY.reviewCount,
            Expressions.constant(0.0)
        ))
        .from(MEMBER)
        .leftJoin(PORTFOLIO).on(PORTFOLIO.member.eq(MEMBER))
        .innerJoin(AGENT_PERFORMANCE_SUMMARY).on(MEMBER.eq(AGENT_PERFORMANCE_SUMMARY.agent))
        .where(MEMBER.memberId.in(agentIds))
        .fetch();
  }

  /**
   * 키워드를 사용하여 닉네임, 한줄 소개에 대해 LIKE 검색을 수행하고 일치하는 대리인 ID 목록을 반환합니다.
   *
   * @param keyword 검색할 키워드
   * @param limit   반환할 결과의 수
   * @return 일치하는 대리인의 UUID 리스트
   */
  @Override
  public List<UUID> findAgentIdsByKeyword(String keyword, int limit) {
    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        MEMBER.memberType.eq(MemberType.AGENT),
        QueryDslUtil.anyOf(
            QueryDslUtil.likeIgnoreCase(MEMBER.nickname, keyword),
            QueryDslUtil.likeIgnoreCase(PORTFOLIO.portfolioDescription, keyword)
        )
    );

    return queryFactory
        .select(MEMBER.memberId)
        .from(MEMBER)
        .innerJoin(PORTFOLIO)
        .on((PORTFOLIO.member).eq(MEMBER))
        .where(whereClause)
        .limit(limit)
        .fetch();
  }

  /**
   * 키워드를 사용하여 공연명, 공연장명, 공연 타입에 대해 LIKE 검색을 수행하고 일치하는 공연 ID 목록을 반환합니다.
   *
   * @param keyword 검색할 키워드
   * @param limit   반환할 결과의 수
   * @return 일치하는 공연의 UUID 리스트
   */
  @Override
  public List<UUID> findConcertIdsByKeyword(String keyword, int limit) {
    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.anyOf(
        QueryDslUtil.likeIgnoreCase(CONCERT.concertName, keyword),
        QueryDslUtil.likeIgnoreCase(CONCERT_HALL.concertHallName, keyword),
        QueryDslUtil.likeIgnoreCase(CONCERT.concertType.stringValue(), keyword)
    );
    return queryFactory
        .select(CONCERT.concertId)
        .from(CONCERT)
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        .where(whereClause)
        .limit(limit)
        .fetch();
  }
}
