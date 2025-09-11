package com.ticketmate.backend.concert.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.concert.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concert.core.constant.ConcertAgentAvailabilitySortField;
import com.ticketmate.backend.concert.infrastructure.entity.QConcertAgentAvailability;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QAgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertAgentAvailabilityRepositoryImpl implements ConcertAgentAvailabilityRepositoryCustom {

  private static final QConcertAgentAvailability CONCERT_AGENT_AVAILABILITY = QConcertAgentAvailability.concertAgentAvailability;
  private static final QMember AGENT = QMember.member;
  private static final QAgentPerformanceSummary AGENT_PERFORMANCE_SUMMARY = QAgentPerformanceSummary.agentPerformanceSummary;

  private final JPAQueryFactory queryFactory;

  /**
   * 특정 콘서트(concertId)에 대해 대리인의 수락 가능 여부를 조회하고 정렬/페이징 처리하여 반환
   *
   * @param concertId 조회 대상 콘서트
   * @param pageable  페이지 번호, 크기, 정렬 정보를 담은 Pageable
   * @return DTO {@link ConcertAcceptingAgentInfo} Slice
   */
  @Override
  public Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, Pageable pageable) {

    JPAQuery<ConcertAcceptingAgentInfo> contentQuery = queryFactory
        .select(Projections.constructor(
            ConcertAcceptingAgentInfo.class,
            AGENT.memberId,
            AGENT.nickname,
            AGENT.profileImgStoredPath,
            CONCERT_AGENT_AVAILABILITY.introduction,
            AGENT_PERFORMANCE_SUMMARY.averageRating,
            AGENT_PERFORMANCE_SUMMARY.reviewCount
        ))
        .from(CONCERT_AGENT_AVAILABILITY)
        .join(CONCERT_AGENT_AVAILABILITY.agent, AGENT)
        .innerJoin(AGENT_PERFORMANCE_SUMMARY).on(AGENT.eq(AGENT_PERFORMANCE_SUMMARY.agent))
        .where(
            CONCERT_AGENT_AVAILABILITY.concert.concertId.eq(concertId),
            CONCERT_AGENT_AVAILABILITY.accepting.isTrue()
        );

    ComparableExpression<Long> followerCountExpression = Expressions.comparableTemplate(
        Long.class,
        "{0}",
        AGENT.followerCount
    );

    // 커스텀 정렬 Map (followerCount 매핑)
    Map<String, ComparableExpression<?>> customSortMap = Collections.singletonMap(
        ConcertAgentAvailabilitySortField.FOLLOWER_COUNT.getProperty(),
        followerCountExpression
    );

    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        AgentPerformanceSummary.class,
        AGENT_PERFORMANCE_SUMMARY.getMetadata().getName(),
        customSortMap
    );

    return QueryDslUtil.fetchSlice(contentQuery, pageable);
  }
}
