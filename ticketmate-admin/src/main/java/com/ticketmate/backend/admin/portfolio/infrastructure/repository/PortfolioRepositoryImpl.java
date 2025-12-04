package com.ticketmate.backend.admin.portfolio.infrastructure.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioAdminInfo;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioFilteredAdminInfo;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.QPortfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.QPortfolioImg;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

  private static final QPortfolio PORTFOLIO = QPortfolio.portfolio;
  private static final QPortfolioImg PORTFOLIO_IMG = QPortfolioImg.portfolioImg;
  private static final QMember MEMBER = QMember.member;

  private final JPAQueryFactory queryFactory;

  /**
   * 포트폴리오 필터링 조회
   *
   * @param username        이메일
   * @param nickname        닉네임
   * @param name            이름
   * @param portfolioStatus 포트폴리오 타입
   */
  @Override
  public Page<PortfolioFilteredAdminInfo> filteredPortfolio(
      String username,
      String nickname,
      String name,
      PortfolioStatus portfolioStatus,
      Pageable pageable) {

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(PORTFOLIO.member.username, username),
        QueryDslUtil.likeIgnoreCase(PORTFOLIO.member.nickname, nickname),
        QueryDslUtil.likeIgnoreCase(PORTFOLIO.member.name, name),
        QueryDslUtil.eqIfNotNull(PORTFOLIO.portfolioStatus, portfolioStatus)
    );

    // 쿼리 작성
    JPAQuery<PortfolioFilteredAdminInfo> contentQuery = queryFactory
        .select(Projections.constructor(PortfolioFilteredAdminInfo.class,
            PORTFOLIO.portfolioId,
            PORTFOLIO.member.memberId,
            PORTFOLIO.member.username,
            PORTFOLIO.member.nickname,
            PORTFOLIO.member.name,
            PORTFOLIO.portfolioStatus,
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

  @Override
  public PortfolioAdminInfo findPortfolioAdminInfoByPortfolioId(UUID portfolioId) {
    List<Tuple> rows = queryFactory
        .select(
            PORTFOLIO.portfolioId,
            MEMBER.memberId,
            MEMBER.nickname,
            MEMBER.phone,
            MEMBER.profileImgStoredPath,
            MEMBER.memberType,
            PORTFOLIO.portfolioDescription,
            PORTFOLIO_IMG.storedPath,
            PORTFOLIO.createdDate,
            PORTFOLIO.updatedDate
        )
        .from(PORTFOLIO)
        .leftJoin(PORTFOLIO.member, MEMBER)
        .leftJoin(PORTFOLIO.portfolioImgList, PORTFOLIO_IMG)
        .where(PORTFOLIO.portfolioId.eq(portfolioId))
        .orderBy(PORTFOLIO_IMG.createdDate.asc())
        .fetch();
    if (rows.isEmpty()) {
      throw new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND);
    }

    Tuple firstRow = rows.get(0);

    List<String> imgStoredPathList = rows.stream()
        .map(tuple -> tuple.get(PORTFOLIO_IMG.storedPath))
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());

    return new PortfolioAdminInfo(
        firstRow.get(PORTFOLIO.portfolioId),
        firstRow.get(MEMBER.memberId),
        firstRow.get(MEMBER.nickname),
        firstRow.get(MEMBER.phone),
        firstRow.get(MEMBER.profileImgStoredPath),
        firstRow.get(MEMBER.memberType),
        firstRow.get(PORTFOLIO.portfolioDescription),
        imgStoredPathList,
        firstRow.get(PORTFOLIO.createdDate),
        firstRow.get(PORTFOLIO.updatedDate)
    );
  }
}
