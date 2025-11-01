package com.ticketmate.backend.concertagentavailability.infrastructure.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.infrastructure.entity.QApplicationForm;
import com.ticketmate.backend.concert.infrastructure.entity.QConcert;
import com.ticketmate.backend.concert.infrastructure.entity.QTicketOpenDate;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAgentStatusInfo;
import com.ticketmate.backend.concertagentavailability.core.constant.ConcertAgentAvailabilitySortField;
import com.ticketmate.backend.concertagentavailability.infrastructure.entity.QConcertAgentAvailability;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QAgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;

import java.time.Instant;
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
  private static final QTicketOpenDate TICKET_OPEN_DATE = QTicketOpenDate.ticketOpenDate;
  private static final QConcert CONCERT = QConcert.concert;
  private static final QApplicationForm APPLICATION_FORM = QApplicationForm.applicationForm;

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

  // TODO: dto명, 메서드명 수정
  @Override
  public Slice<ConcertAgentStatusInfo> findMyConcertList(UUID agentId, Pageable pageable) {
    Instant now = Instant.now();

    // status (모집 중/마감)
    NumberExpression<Integer> statusExpression = new CaseBuilder()
        .when(JPAExpressions.selectOne()
            .from(TICKET_OPEN_DATE)
            .where(
                TICKET_OPEN_DATE.concert.eq(CONCERT),
                TICKET_OPEN_DATE.openDate.gt(now)
            ).exists())
        .then(1)
        .otherwise(2);

    // matchedClientCount (매칭된 의뢰인 수)
    Expression<Integer> matchedClientCountExpression = JPAExpressions
        .select(APPLICATION_FORM.count().intValue())
        .from(APPLICATION_FORM)
        .where(
            APPLICATION_FORM.concert.eq(CONCERT),
            APPLICATION_FORM.agent.memberId.eq(agentId),
            APPLICATION_FORM.applicationFormStatus.eq(ApplicationFormStatus.APPROVED)
        );

    // accepting 여부
    BooleanExpression acceptingExpression = CONCERT_AGENT_AVAILABILITY.accepting.isTrue().coalesce(false);

    JPAQuery<ConcertAgentStatusInfo> contentQuery = queryFactory
        .select(Projections.constructor(
            ConcertAgentStatusInfo.class,
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT.concertThumbnailStoredPath,
            statusExpression,
            matchedClientCountExpression,
            acceptingExpression
        ))
        .from(CONCERT)
        .leftJoin(CONCERT_AGENT_AVAILABILITY)
        .on(
            CONCERT_AGENT_AVAILABILITY.concert.eq(CONCERT),
            CONCERT_AGENT_AVAILABILITY.agent.memberId.eq(agentId)
        );

    // 정렬
    contentQuery.orderBy(
        statusExpression.asc(),     // 모집 여부
        acceptingExpression.desc(), // accepting 여부
        CONCERT.createdDate.desc()  // 최신순
    );

    return QueryDslUtil.fetchSlice(contentQuery, pageable);
  }
}
