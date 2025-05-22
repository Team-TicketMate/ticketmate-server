package com.ticketmate.backend.repository.postgres.concert;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.postgres.concert.QConcert;
import com.ticketmate.backend.object.postgres.concert.QConcertDate;
import com.ticketmate.backend.object.postgres.concert.QTicketOpenDate;
import com.ticketmate.backend.object.postgres.concerthall.QConcertHall;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 공연 필터링 조회
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

        // 동적 WHERE 조건 조합
        BooleanExpression whereClause = null; // 초기값 null
        whereClause = combineWhereClause(whereClause, whereConcertName(concertName));
        whereClause = combineWhereClause(whereClause, whereConcertHallName(concertHallName));
        whereClause = combineWhereClause(whereClause, whereConcertType(concertType));
        whereClause = combineWhereClause(whereClause, whereTicketReservationSite(ticketReservationSite));

        // 쿼리 작성
        JPAQuery<ConcertFilteredResponse> query = queryFactory
                .select(Projections.constructor(ConcertFilteredResponse.class,
                        concert.concertId,
                        concert.concertName,
                        concertHall.concertHallName,
                        concert.concertType,
                        concert.ticketReservationSite,
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
                .leftJoin(concert.concertHall, concertHall)
                .leftJoin(concertDate).on(concert.concertId.eq(concertDate.concert.concertId))
                .leftJoin(ticketOpenDate).on(concert.concertId.eq(ticketOpenDate.concert.concertId))
                .where(whereClause)
                .groupBy(concert.concertId,
                        concert.concertName,
                        concertHall.concertHallName,
                        concert.concertType,
                        concert.ticketReservationSite,
                        concert.concertThumbnailUrl,
                        concert.seatingChartUrl
                );


        // 정렬 적용
        query.orderBy(getOrderSpecifier(concert, ticketOpenDate, pageable.getSort()));

        // 페이징 적용
        List<ConcertFilteredResponse> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(concert.concertId.countDistinct())
                .from(concert)
                .leftJoin(concert.concertHall, concertHall)
                .leftJoin(concertDate).on(concert.concertId.eq(concertDate.concert.concertId))
                .leftJoin(ticketOpenDate).on(concert.concertId.eq(ticketOpenDate.concert.concertId))
                .where(whereClause)
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * WHERE 절 조합 메서드
     * base절과 additional절을 AND 로 조합
     *
     * @param baseClause       기존 WHERE 절
     * @param additionalClause 추가 WHERE 절
     * @return 조합된 최종 WHERE 절
     */
    private BooleanExpression combineWhereClause(BooleanExpression baseClause, BooleanExpression additionalClause) {
        if (additionalClause == null) {
            return baseClause; // 추가 조건이 없으면 기존 조건 유지
        }
        if (baseClause == null) {
            return additionalClause; // 기존 조건이 없으면 추가 조건을 기본으로 설정
        }
        return baseClause.and(additionalClause); // 두 조건을 AND로 조합 후 반환
    }

    // 동적 WHERE 조건 메서드
    private BooleanExpression whereConcertName(String concertName) {
        return concertName.trim().isEmpty() ?
                null : QConcert.concert.concertName.lower().like("%" + concertName.toLowerCase() + "%");
    }

    private BooleanExpression whereConcertHallName(String concertHallName) {
        return concertHallName.trim().isEmpty() ?
                null : QConcertHall.concertHall.concertHallName.lower().like("%" + concertHallName.toLowerCase() + "%");
    }

    private BooleanExpression whereConcertType(ConcertType concertType) {
        return concertType == null ?
                null : QConcert.concert.concertType.eq(concertType);
    }

    private BooleanExpression whereTicketReservationSite(TicketReservationSite ticketReservationSite) {
        return ticketReservationSite == null ?
                null : QConcert.concert.ticketReservationSite.eq(ticketReservationSite);
    }

    // 정렬 로직
    private OrderSpecifier<?> getOrderSpecifier(QConcert concert, QTicketOpenDate ticketOpenDate, Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return concert.createdDate.desc(); // 기본 정렬
        }

        Sort.Order order = sort.iterator().next(); // sort 내부에서 order 가져옴
        String sortField = order.getProperty(); // sortField
        boolean isAsc = order.isAscending(); // 오름차순 여부

        return switch (sortField) {
            case "created_date" -> isAsc ? concert.createdDate.asc() : concert.createdDate.desc(); // 시간순 or 최신순
            case "ticket_open_date" -> isAsc ? ticketOpenDate.openDate.min().asc() : ticketOpenDate.openDate.min().desc(); // 티켓 오픈일 빠른순 or 느린순
            default -> concert.createdDate.desc(); // 기본 정렬
        };
    }
}
