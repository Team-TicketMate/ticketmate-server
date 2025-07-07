package com.ticketmate.backend.domain.concert.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.concert.domain.constant.ConcertType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketReservationSite;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.QConcert;
import com.ticketmate.backend.domain.concert.domain.entity.QConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.QTicketOpenDate;
import com.ticketmate.backend.domain.concerthall.domain.entity.QConcertHall;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  /**
   * 공연 필터링 조회 (JOIN + GROUP BY)
   *
   * @param concertName           공연 이름 (검색어)
   * @param concertHallName       공연장 이름 (검색어)
   * @param concertType           공연 카테고리
   * @param ticketReservationSite 예매처
   * @return concertFilteredResponse
   */
  @Override
  public Page<ConcertFilteredResponse> filteredConcert(
      String concertName,
      String concertHallName,
      ConcertType concertType,
      TicketReservationSite ticketReservationSite,
      Pageable pageable) {

    QConcert concert = QConcert.concert;
    QConcertHall concertHall = QConcertHall.concertHall;
    QConcertDate concertDate = QConcertDate.concertDate;
    QTicketOpenDate ticketOpenDate = QTicketOpenDate.ticketOpenDate;

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(concert.concertName, concertName),
        QueryDslUtil.likeIgnoreCase(concertHall.concertHallName, concertHallName),
        QueryDslUtil.eqIfNotNull(concert.concertType, concertType),
        QueryDslUtil.eqIfNotNull(concert.ticketReservationSite, ticketReservationSite)
    );

    // 쿼리 작성 (JOIN + GROUP BY + CASE WHEN)
    JPAQuery<ConcertFilteredResponse> contentQuery = queryFactory
        .select(Projections.constructor(ConcertFilteredResponse.class,
            concert.concertId,
            concert.concertName,
            concertHall.concertHallName,
            concert.concertType,
            concert.ticketReservationSite,

            // 성능 최적화를 위한 CASE WHEN
            // 선예매 오픈일
            Expressions.dateTimeTemplate(
                LocalDateTime.class,
                "min({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
                    .then(ticketOpenDate.openDate)
                    .otherwise((LocalDateTime) null)
            ).as("ticketPreOpenDate"),
            // 선예매 무통장 여부
            Expressions.booleanTemplate(
                "bool_or({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
                    .then((ComparableExpression<Boolean>) ticketOpenDate.isBankTransfer)
                    .otherwise((Boolean) null)
            ).as("preOpenBankTransfer"),
            // 일반 예매 오픈일
            Expressions.dateTimeTemplate(
                LocalDateTime.class,
                "min({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
                    .then(ticketOpenDate.openDate)
                    .otherwise((LocalDateTime) null)
            ).as("ticketGeneralOpenDate"),
            // 일반 예매 무통장 여부
            Expressions.booleanTemplate(
                "bool_or({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
                    .then((ComparableExpression<Boolean>) ticketOpenDate.isBankTransfer)
                    .otherwise((Boolean) null)
            ).as("generalOpenBankTransfer"),
            concertDate.performanceDate.min().as("startDate"), // 공연 시작일
            concertDate.performanceDate.max().as("endDate"), // 공연 종료일
            concert.concertThumbnailUrl,
            concert.seatingChartUrl
        ))
        .from(concert)
        // 공연장은 필수가 아니므로 Left Join
        .leftJoin(concert.concertHall, concertHall)
        // 공연일(ConcertDate), 티켓오픈일(TicketOpenDate)은 필수이므로 Inner Join
        .join(concertDate).on(concert.concertId.eq(concertDate.concert.concertId))
        .join(ticketOpenDate).on(concert.concertId.eq(ticketOpenDate.concert.concertId))
        .where(whereClause)
        .groupBy(concert.concertId,
            concert.concertName,
            concertHall.concertHallName,
            concert.concertType,
            concert.ticketReservationSite,
            concert.concertThumbnailUrl,
            concert.seatingChartUrl
        );

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        Concert.class,
        concert.getMetadata().getName()
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(concert.count())
        .from(concert)
        .leftJoin(concert.concertHall, concertHall)
        .where(whereClause);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }
}
