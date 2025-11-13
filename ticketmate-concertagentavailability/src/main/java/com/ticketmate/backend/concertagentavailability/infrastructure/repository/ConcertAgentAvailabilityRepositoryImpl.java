package com.ticketmate.backend.concertagentavailability.infrastructure.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.infrastructure.entity.QApplicationForm;
import com.ticketmate.backend.concert.infrastructure.entity.QConcert;
import com.ticketmate.backend.concert.infrastructure.entity.QTicketOpenDate;
import com.ticketmate.backend.concertagentavailability.application.dto.view.AgentConcertSettingInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.core.constant.ConcertAgentAvailabilitySortField;
import com.ticketmate.backend.concertagentavailability.infrastructure.entity.QConcertAgentAvailability;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QAgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Slf4j
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

  /**
   * 대리인 마이페이지용 on/off 설정을 위한 전체 공연 조회
   * @param agentId 로그인한 대리인
   * @param pageable 페이지 번호, 크기를 담은 Pageable
   * @return DTO {@link AgentConcertSettingInfo} Slice
   */
  @Override
  public Slice<AgentConcertSettingInfo> findMyConcertList(UUID agentId, Pageable pageable) {
    Instant now = Instant.now();

    Expression<Integer> matchedClientCountExpression = getMatchedClientCountExpression(agentId);
    BooleanExpression acceptingExpression = getAcceptingExpression();

    JPAQuery<AgentConcertSettingInfo> contentQuery = createBaseConcertStatusQuery(
        agentId, now, matchedClientCountExpression, acceptingExpression
    );

    // 정렬
    contentQuery.orderBy(
        acceptingExpression.desc(), // accepting 여부
        CONCERT.createdDate.desc()  // 최신순
    );

    return QueryDslUtil.fetchSlice(contentQuery, pageable);
  }

  /**
   * 대리인 마이페이지용 on 설정한 모집 중 공연 조회
   * @param agentId 로그인한 대리인
   * @return DTO {@link AgentConcertSettingInfo} List
   */
  @Override
  public List<AgentConcertSettingInfo> findMyAcceptingConcert(UUID agentId) {
    Instant now = Instant.now();

    Expression<Integer> matchedClientCountExpression = getMatchedClientCountExpression(agentId);
    BooleanExpression acceptingExpression = getAcceptingExpression();

    JPAQuery<AgentConcertSettingInfo> contentQuery = createBaseConcertStatusQuery(
        agentId, now, matchedClientCountExpression, acceptingExpression
    );

    contentQuery
        // 필터링
        .where(acceptingExpression.isTrue()) // ON
        // 정렬
        .orderBy(CONCERT.createdDate.desc()) // 최신순
        .limit(10); // 최대 10개

    return contentQuery.fetch();
  }

  /**
   * 대리인 마이페이지 on/off 설정 공연 조회용 기본 쿼리
   */
  private JPAQuery<AgentConcertSettingInfo> createBaseConcertStatusQuery(
      UUID agentId,
      Instant now,
      Expression<Integer> matchedClientCountExpression,
      BooleanExpression acceptingExpression
  ) {
    BooleanExpression isRecruiting = JPAExpressions.selectOne()
        .from(TICKET_OPEN_DATE)
        .where(
            TICKET_OPEN_DATE.concert.eq(CONCERT),
            TICKET_OPEN_DATE.openDate.gt(now)
        ).exists();

    return queryFactory
        .select(Projections.constructor(
            AgentConcertSettingInfo.class,
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT.concertThumbnailStoredPath,
            matchedClientCountExpression,
            acceptingExpression
        ))
        .from(CONCERT)
        .leftJoin(CONCERT_AGENT_AVAILABILITY)
        .on(
            CONCERT_AGENT_AVAILABILITY.concert.eq(CONCERT),
            CONCERT_AGENT_AVAILABILITY.agent.memberId.eq(agentId)
        )
        .where(isRecruiting);
  }

  /**
   * matchedClientCount (매칭된 의뢰인 수) 표현식
   */
  private Expression<Integer> getMatchedClientCountExpression(UUID agentId) {
    return JPAExpressions
        .select(APPLICATION_FORM.count().intValue())
        .from(APPLICATION_FORM)
        .where(
            APPLICATION_FORM.concert.eq(CONCERT),
            APPLICATION_FORM.agent.memberId.eq(agentId),
            APPLICATION_FORM.applicationFormStatus.eq(ApplicationFormStatus.APPROVED)
        );
  }

  /**
   * accepting (ON/OFF) 표현식
   */
  private BooleanExpression getAcceptingExpression() {
    return CONCERT_AGENT_AVAILABILITY.accepting.isTrue().coalesce(false);
  }
}
