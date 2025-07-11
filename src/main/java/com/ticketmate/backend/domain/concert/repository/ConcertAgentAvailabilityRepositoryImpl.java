package com.ticketmate.backend.domain.concert.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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
  private final QConcertAgentAvailability concertAgentAvailability = QConcertAgentAvailability.concertAgentAvailability;
  private final QMember member = QMember.member;
  private final QAgentPerformanceSummary agentPerformanceSummary = QAgentPerformanceSummary.agentPerformanceSummary;


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
        .innerJoin(agentPerformanceSummary).on(member.memberId.eq(agentPerformanceSummary.agentId))
        .where(
            concertAgentAvailability.concert.concertId.eq(concertId),
            concertAgentAvailability.accepting.isTrue()
        );

    Sort sort = pageable.getSort();
    Sort.Order order = sort.iterator().next();
    String property = order.getProperty();
    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

    OrderSpecifier<?> primarySpecifier = switch(property){
      case "totalScore" -> new OrderSpecifier<>(direction, agentPerformanceSummary.totalScore);
      case "averageRating" -> new OrderSpecifier<>(direction, agentPerformanceSummary.averageRating);
      case "reviewCount" -> new OrderSpecifier<>(direction, agentPerformanceSummary.reviewCount);
      case "followerCount" -> new OrderSpecifier<>(direction, agentPerformanceSummary.followerCount);
      case "recentSuccessCount" -> new OrderSpecifier<>(direction, agentPerformanceSummary.recentSuccessCount);
      default -> throw new CustomException(ErrorCode.INVALID_SORT_FIELD);
    };
    contentQuery.orderBy(primarySpecifier, new OrderSpecifier<>(direction, member.createdDate));

    return QueryDslUtil.fetchSlice(contentQuery, pageable);
  }
}
