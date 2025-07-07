package com.ticketmate.backend.domain.portfolio.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.domain.member.domain.entity.QMember;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.domain.entity.QPortfolio;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

  private final JPAQueryFactory queryFactory;

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

    QPortfolio portfolio = QPortfolio.portfolio;
    QMember member = QMember.member;

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(portfolio.member.username, username),
        QueryDslUtil.likeIgnoreCase(portfolio.member.nickname, nickname),
        QueryDslUtil.likeIgnoreCase(portfolio.member.name, name),
        QueryDslUtil.eqIfNotNull(portfolio.portfolioType, portfolioType)
    );

    // 쿼리 작성
    JPAQuery<PortfolioFilteredAdminResponse> contentQuery = queryFactory
        .select(Projections.constructor(PortfolioFilteredAdminResponse.class,
            portfolio.portfolioId,
            portfolio.member.memberId,
            portfolio.member.username,
            portfolio.member.nickname,
            portfolio.member.name,
            portfolio.portfolioType,
            portfolio.createdDate,
            portfolio.updatedDate
        ))
        .from(portfolio)
        .innerJoin(portfolio.member, member)
        .where(whereClause);

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        Portfolio.class,
        portfolio.getMetadata().getName(
        ));

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(portfolio.count())
        .from(portfolio)
        .where(whereClause);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }
}
