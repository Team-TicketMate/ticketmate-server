package com.ticketmate.backend.admin.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.admin.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.QPortfolio;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  private static final QPortfolio PORTFOLIO = QPortfolio.portfolio;
  private static final QMember MEMBER = QMember.member;

  /**
   * 포트폴리오 필터링 조회
   *
   * @param username      이메일
   * @param nickname      닉네임
   * @param name          이름
   * @param portfolioType 포트폴리오 타입
   */
  @Override
  public Page<PortfolioFilteredAdminResponse> filteredPortfolio(
      String username,
      String nickname,
      String name,
      PortfolioType portfolioType,
      Pageable pageable) {

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(PORTFOLIO.member.username, username),
        QueryDslUtil.likeIgnoreCase(PORTFOLIO.member.nickname, nickname),
        QueryDslUtil.likeIgnoreCase(PORTFOLIO.member.name, name),
        QueryDslUtil.eqIfNotNull(PORTFOLIO.portfolioType, portfolioType)
    );

    // 쿼리 작성
    JPAQuery<PortfolioFilteredAdminResponse> contentQuery = queryFactory
        .select(Projections.constructor(PortfolioFilteredAdminResponse.class,
            PORTFOLIO.portfolioId,
            PORTFOLIO.member.memberId,
            PORTFOLIO.member.username,
            PORTFOLIO.member.nickname,
            PORTFOLIO.member.name,
            PORTFOLIO.portfolioType,
            PORTFOLIO.createdDate,
            PORTFOLIO.updatedDate
        ))
        .from(PORTFOLIO)
        .innerJoin(PORTFOLIO.member, MEMBER)
        .where(whereClause);

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        Portfolio.class,
        PORTFOLIO.getMetadata().getName()
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(PORTFOLIO.count())
        .from(PORTFOLIO)
        .where(whereClause);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }
}
