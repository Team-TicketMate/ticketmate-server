package com.ticketmate.backend.domain.concert.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.domain.entity.QConcertAgentAvailability;
import com.ticketmate.backend.domain.member.domain.entity.QAgentPerformanceSummary;
import com.ticketmate.backend.domain.member.domain.entity.QMember;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertAgentAvailabilityRepositoryImpl implements ConcertAgentAvailabilityRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private static final QConcertAgentAvailability concertAgentAvailability = QConcertAgentAvailability.concertAgentAvailability;
  private static final QMember member = QMember.member;
  private static final QAgentPerformanceSummary agentPerformanceSummary = QAgentPerformanceSummary.agentPerformanceSummary;

  // 정렬 가능한 속성과 Q-Type 경로를 매핑
  private static final Map<String, Path<?>> SORTABLE_PROPERTIES = Map.of(
      "totalScore", agentPerformanceSummary.totalScore,
      "averageRating", agentPerformanceSummary.averageRating,
      "reviewCount", agentPerformanceSummary.reviewCount,
      "followerCount", agentPerformanceSummary.followerCount,
      "recentSuccessCount", agentPerformanceSummary.recentSuccessCount
  );

  /**
   * 특정 콘서트(concertId)에 대해 대리인의 수락 가능 여부를 조회하고 정렬/페이징 처리하여 반환
   *
   * @param concertId  조회 대상 콘서트
   * @param pageable   페이지 번호, 크기, 정렬 정보를 담은 Pageable
   * @return DTO {@link ConcertAcceptingAgentInfo} Slice
   */
  @Override
  public Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, Pageable pageable){

    JPAQuery<ConcertAcceptingAgentInfo> contentQuery = queryFactory
        .select(Projections.constructor(
            ConcertAcceptingAgentInfo.class,
            member.memberId,
            member.nickname,
            member.profileUrl,
            concertAgentAvailability.introduction,
            agentPerformanceSummary.averageRating,
            agentPerformanceSummary.reviewCount
        ))
        .from(concertAgentAvailability)
        .join(concertAgentAvailability.agent, member)
        .innerJoin(agentPerformanceSummary).on(member.eq(agentPerformanceSummary.agent))
        .where(
            concertAgentAvailability.concert.concertId.eq(concertId),
            concertAgentAvailability.accepting.isTrue()
        );

    OrderSpecifier<?>[] orderSpecifiers = QueryDslUtil.createOrderSpecifiers(
        pageable,
        SORTABLE_PROPERTIES,
        List.of((direction) -> new OrderSpecifier<>(direction, member.createdDate)));
    contentQuery.orderBy(orderSpecifiers);

    return QueryDslUtil.fetchSlice(contentQuery, pageable);
  }
}
