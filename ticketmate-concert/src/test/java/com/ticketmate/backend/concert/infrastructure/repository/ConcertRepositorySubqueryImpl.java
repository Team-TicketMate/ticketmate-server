//package com.ticketmate.backend.service.performance;
//
//import com.querydsl.core.types.OrderSpecifier;
//import com.querydsl.core.types.Projections;
//import com.querydsl.core.types.dsl.BooleanExpression;
//import com.querydsl.core.types.dsl.DateTimeExpression;
//import com.querydsl.core.types.dsl.Expressions;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import com.ticketmate.backend.concert.core.constant.ConcertType;
//import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
//import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
//import com.ticketmate.backend.domain.concert.domain.entity.QConcert;
//import com.ticketmate.backend.domain.concert.repository.ConcertRepositoryCustom;
//import com.ticketmate.backend.domain.concerthall.domain.entity.QConcertHall;
//import java.time.LocalDateTime;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Repository;
//
//@Repository
//@RequiredArgsConstructor
//public class ConcertRepositorySubqueryImpl implements ConcertRepositoryCustom {
//
//  private final JPAQueryFactory queryFactory;
//
//  /**
//   * 공연 필터링 조회 (서브쿼리)
//   *
//   * @param concertName           공연 이름 (검색어)
//   * @param concertHallName       공연장 이름 (검색어)
//   * @param concertType           공연 카테고리
//   * @param ticketReservationSite 예매처
//   * @return concertFilteredResponse
//   */
//  @Override
//  public Page<ConcertFilteredResponse> filteredConcert(
//      String concertName,
//      String concertHallName,
//      ConcertType concertType,
//      TicketReservationSite ticketReservationSite,
//      Pageable pageable) {
//
//    QConcert concert = QConcert.concert;
//    QConcertHall concertHall = QConcertHall.concertHall;
//
//    // 동적 WHERE 절 조합
//    BooleanExpression whereClause = null;
//    whereClause = combineWhereClause(whereClause, whereConcertName(concertName));
//    whereClause = combineWhereClause(whereClause, whereConcertHallName(concertHallName));
//    whereClause = combineWhereClause(whereClause, whereConcertType(concertType));
//    whereClause = combineWhereClause(whereClause, whereTicketReservationSite(ticketReservationSite));
//
//    // ticketOpenDateSubquery: 티켓 오픈일 (PRE_OPEN + GENERAL_OPEN 중에서 가장 빠른 일자)
//    DateTimeExpression<LocalDateTime> ticketOpenDateSubquery =
//        Expressions.dateTimeTemplate(
//            LocalDateTime.class,
//            """
//                (
//                select min (tod.openDate)
//                from ticketOpenDate tod
//                where tod.concert.id = {0}
//                )
//                """,
//            concert.concertId
//        );
//
//    // 메인 쿼리: Concert + ConcertHall Join
//    JPAQuery<ConcertFilteredResponse> query = queryFactory
//        .select(Projections.constructor(ConcertFilteredResponse.class,
//            concert.concertId,
//            concert.concertName,
//            concertHall.concertHallName,
//            concert.concertType,
//            concert.ticketReservationSite,
//
//            // 선얘매 오픈일 (최소값)
//            Expressions.dateTimeTemplate(
//                LocalDateTime.class,
//                """
//                    (
//                    select min(tod.openDate)
//                    from TicketOpenDate tod
//                    where tod.concert.id = {0}
//                    and tod.ticketOpenType = 'PRE_OPEN'
//                    )
//                    """,
//                concert.concertId
//            ).as("ticketPreOpenDate"),
//
//            // 선예매 무통장 여부 (bool_or)
//            Expressions.booleanTemplate(
//                """
//                    (
//                    select bool_or(tod.isBankTransfer)
//                    from TicketOpenDate tod
//                    where tod.concert.id = {0}
//                    and tod.ticketOpenType = 'PRE_OPEN'
//                    )
//                    """,
//                concert.concertId
//            ).as("preOpenBankTransfer"),
//
//            // 일반예매 오픈일 (최소값)
//            Expressions.dateTimeTemplate(
//                LocalDateTime.class,
//                """
//                    (
//                    select min(tod.openDate)
//                    from TicketOpenDate tod
//                    where tod.concert.id = {0}
//                    and tod.ticketOpenType = 'GENERAL_OPEN'
//                    )
//                    """,
//                concert.concertId
//            ).as("ticketGeneralOpenDate"),
//
//            // 일반예매 무통장 여부 (bool_or)
//            Expressions.booleanTemplate(
//                """
//                    (
//                    select bool_or(tod.isBankTransfer)
//                    from TicketOpenDate tod
//                    where tod.concert.id = {0}
//                    and tod.ticketOpenType = 'GENERAL_OPEN'
//                    )
//                    """,
//                concert.concertId
//            ).as("generalOpenBankTransfer"),
//
//            // 공연 시작일 (min)
//            Expressions.dateTimeTemplate(
//                LocalDateTime.class,
//                """
//                    (
//                    select min(cd.performanceDate)
//                    from ConcertDate cd
//                    where cd.concert.id = {0}
//                    )
//                    """,
//                concert.concertId
//            ).as("startDate"),
//
//            // 공연 종료일 (max)
//            Expressions.dateTimeTemplate(
//                LocalDateTime.class,
//                """
//                    (
//                    select max(cd.performanceDate)
//                    from ConcertDate cd
//                    where cd.concert.id = {0}
//                    )
//                    """,
//                concert.concertId
//            ).as("endDate"),
//
//            concert.concertThumbnailUrl,
//            concert.seatingChartUrl
//        ))
//        .from(concert)
//        .join(concert.concertHall, concertHall)
//        .where(whereClause)
//        .orderBy(getOrderSpecifier(concert, ticketOpenDateSubquery, pageable.getSort()))
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize());
//
//    List<ConcertFilteredResponse> content = query.fetch();
//
//    // count 쿼리
//    Long total = queryFactory
//        .select(concert.concertId.count())
//        .from(concert)
//        .where(whereClause)
//        .fetchOne();
//    total = total == null ? 0L : total;
//
//    return new PageImpl<>(content, pageable, total);
//  }
//
//  @Override
//  public Page<ConcertFilteredResponse> filteredConcertForAdmin(String concertName, String concertHallName, ConcertType concertType, TicketReservationSite ticketReservationSite, Pageable pageable) {
//    return null;
//  }
//
//  /**
//   * WHERE 절 조합 메서드
//   * base절과 additional절을 AND 로 조합
//   *
//   * @param baseClause       기존 WHERE 절
//   * @param additionalClause 추가 WHERE 절
//   * @return 조합된 최종 WHERE 절
//   */
//  private BooleanExpression combineWhereClause(BooleanExpression baseClause, BooleanExpression additionalClause) {
//    if (additionalClause == null) {
//      return baseClause; // 추가 조건이 없으면 기존 조건 유지
//    }
//    if (baseClause == null) {
//      return additionalClause; // 기존 조건이 없으면 추가 조건을 기본으로 설정
//    }
//    return baseClause.and(additionalClause); // 두 조건을 AND로 조합 후 반환
//  }
//
//  // 동적 WHERE 조건 메서드
//  private BooleanExpression whereConcertName(String concertName) {
//    return concertName.trim().isEmpty() ?
//        null : QConcert.concert.concertName.lower().like("%" + concertName.toLowerCase() + "%");
//  }
//
//  private BooleanExpression whereConcertHallName(String concertHallName) {
//    return concertHallName.trim().isEmpty() ?
//        null : QConcertHall.concertHall.concertHallName.lower().like("%" + concertHallName.toLowerCase() + "%");
//  }
//
//  private BooleanExpression whereConcertType(ConcertType concertType) {
//    return concertType == null ?
//        null : QConcert.concert.concertType.eq(concertType);
//  }
//
//  private BooleanExpression whereTicketReservationSite(TicketReservationSite ticketReservationSite) {
//    return ticketReservationSite == null ?
//        null : QConcert.concert.ticketReservationSite.eq(ticketReservationSite);
//  }
//
//  // 정렬 로직
//  private OrderSpecifier<?> getOrderSpecifier(QConcert concert, DateTimeExpression<LocalDateTime> ticketOpenDateSubquery, Sort sort) {
//
//    if (sort == null || sort.isEmpty()) {
//      return concert.createdDate.desc(); // 기본 정렬
//    }
//
//    Sort.Order order = sort.iterator().next(); // sort 내부에서 order 가져옴
//    String sortField = order.getProperty(); // sortField
//    boolean isAsc = order.isAscending(); // 오름차순 여부
//
//    return switch (sortField) {
//      case "created_date" -> isAsc ? concert.createdDate.asc() : concert.createdDate.desc(); // 시간순 or 최신순
//      case "ticket_open_date" -> isAsc ? ticketOpenDateSubquery.asc() : ticketOpenDateSubquery.desc(); // 티켓 오픈일 빠른순 or 느린순
//      default -> concert.createdDate.desc(); // 기본 정렬
//    };
//  }
//}