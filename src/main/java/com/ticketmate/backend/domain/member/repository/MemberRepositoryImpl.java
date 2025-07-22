package com.ticketmate.backend.domain.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.entity.QAgentPerformanceSummary;
import com.ticketmate.backend.domain.member.domain.entity.QMember;
import com.ticketmate.backend.domain.portfolio.domain.entity.QPortfolio;
import com.ticketmate.backend.domain.search.domain.dto.response.AgentSearchResponse;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
  private final JPAQueryFactory queryFactory;
  private static final QMember member = QMember.member;
  private static final QPortfolio portfolio = QPortfolio.portfolio;
  private static final QAgentPerformanceSummary agentPerformanceSummary = QAgentPerformanceSummary.agentPerformanceSummary;

  /**
   * 키워드를 사용하여 닉네임, 한줄 소개에 대해 LIKE 검색을 수행하고 일치하는 대리인 ID 목록을 반환합니다.
   *
   * @param keyword 검색할 키워드
   * @param limit 반환할 결과의 수
   * @return 일치하는 대리인의 UUID 리스트
   */
  @Override
  public List<UUID> findAgentIdsByKeyword(String keyword, int limit){
    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        member.memberType.eq(MemberType.AGENT),
        QueryDslUtil.anyOf(
            QueryDslUtil.likeIgnoreCase(member.nickname, keyword),
            QueryDslUtil.likeIgnoreCase(portfolio.portfolioDescription, keyword)
        )
    );

    return queryFactory
        .select(member.memberId)
        .from(member)
        .innerJoin(portfolio)
        .on((portfolio.member).eq(member))
        .where(whereClause)
        .limit(limit)
        .fetch();
  }

  /**
   * 제공된 ID 목록에 해당하는 대리인의 상세 정보를 조회하여 AgentSearchResponse DTO 리스트로 반환합니다.
   *
   * @param agentIds 상세 정보를 조회할 대리인의 UUID 리스트
   * @return 대리인 상세 정보가 담긴 DTO 리스트
   */
  @Override
  public List<AgentSearchResponse> findAgentDetailsByIds(List<UUID> agentIds){
    if (agentIds == null || agentIds.isEmpty()) {
      return Collections.emptyList();
    }
    return queryFactory
        .select(Projections.constructor(AgentSearchResponse.class,
            member.memberId,
            member.nickname,
            member.profileUrl,
            portfolio.portfolioDescription,
            agentPerformanceSummary.averageRating,
            agentPerformanceSummary.reviewCount
        ))
        .from(member)
        .leftJoin(portfolio).on(portfolio.member.eq(member))
        .innerJoin(agentPerformanceSummary).on(member.eq(agentPerformanceSummary.agent))
        .where(member.memberId.in(agentIds))
        .fetch();
  }
}
