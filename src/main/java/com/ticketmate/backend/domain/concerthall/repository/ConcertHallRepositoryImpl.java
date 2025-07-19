package com.ticketmate.backend.domain.concerthall.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.concerthall.domain.entity.QConcertHall;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertHallRepositoryImpl implements ConcertHallRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  private static final QConcertHall CONCERT_HALL = QConcertHall.concertHall;

  /**
   * 공연장 필터링 조회
   *
   * @param concertHallName 공연장 이름
   * @param city            지역
   */
  @Override
  public Page<ConcertHallFilteredResponse> filteredConcertHall(
      String concertHallName,
      City city,
      Pageable pageable) {

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(CONCERT_HALL.concertHallName, concertHallName),
        QueryDslUtil.eqIfNotNull(CONCERT_HALL.city, city)
    );

    // 쿼리 작성
    JPAQuery<ConcertHallFilteredResponse> contentQuery = queryFactory
        .select(Projections.constructor(ConcertHallFilteredResponse.class,
            CONCERT_HALL.concertHallId,
            CONCERT_HALL.concertHallName,
            CONCERT_HALL.address,
            CONCERT_HALL.webSiteUrl
        ))
        .from(CONCERT_HALL)
        .where(whereClause);

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        ConcertHall.class,
        CONCERT_HALL.getMetadata().getName()
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(CONCERT_HALL.count())
        .from(CONCERT_HALL)
        .where(whereClause);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }
}
