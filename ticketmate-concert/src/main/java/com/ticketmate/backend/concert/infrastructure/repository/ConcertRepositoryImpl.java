package com.ticketmate.backend.concert.infrastructure.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concert.application.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.concert.core.constant.ConcertSortField;
import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.QConcert;
import com.ticketmate.backend.concert.infrastructure.entity.QConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.QTicketOpenDate;
import com.ticketmate.backend.concerthall.infrastructure.entity.QConcertHall;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {

  private static final QConcert CONCERT = QConcert.concert;
  private static final QConcertHall CONCERT_HALL = QConcertHall.concertHall;
  private static final QConcertDate CONCERT_DATE = QConcertDate.concertDate;
  private static final QTicketOpenDate TICKET_OPEN_DATE = QTicketOpenDate.ticketOpenDate;
  private final JPAQueryFactory queryFactory;
  private final ZoneId zone = ZoneId.of("Asia/Seoul");

  /**
   * 공연 상세 조회
   */
  @Override
  public ConcertInfoResponse findConcertInfoResponseByConcertId(UUID concertId) {

    LocalDateTime now = LocalDateTime.now(zone);

    List<Tuple> rows = queryFactory
        .select(
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertThumbnailUrl,
            CONCERT.seatingChartUrl,
            CONCERT.concertType,
            TICKET_OPEN_DATE.openDate,
            TICKET_OPEN_DATE.requestMaxCount,
            TICKET_OPEN_DATE.isBankTransfer,
            TICKET_OPEN_DATE.ticketOpenType,
            CONCERT_DATE.performanceDate,
            CONCERT_DATE.session,
            CONCERT.ticketReservationSite
        )
        .from(CONCERT)
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        .leftJoin(CONCERT_DATE).on(CONCERT_DATE.concert.eq(CONCERT))
        .leftJoin(TICKET_OPEN_DATE).on(TICKET_OPEN_DATE.concert.eq(CONCERT))
        .where(CONCERT.concertId.eq(concertId))
        .fetch();
    if (rows.isEmpty()) {
      throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    // 1) 공통 ConcertInfo 필드 추출 (rows 가 비어있지 않다고 가정)
    Tuple firstRow = rows.get(0);
    ConcertInfoResponse concertInfoResponse = ConcertInfoResponse.builder()
        .concertName(firstRow.get(CONCERT.concertName))
        .concertHallName(firstRow.get(CONCERT_HALL.concertHallName))
        .concertThumbnailUrl(firstRow.get(CONCERT.concertThumbnailUrl))
        .seatingChartUrl(firstRow.get(CONCERT.seatingChartUrl))
        .concertType(firstRow.get(CONCERT.concertType))
        .ticketReservationSite(firstRow.get(CONCERT.ticketReservationSite))
        .build();

    // —— 공연 일자 중복 제거 (key: session) —— //
    Map<Integer, ConcertDateInfoResponse> dateMap = new LinkedHashMap<>();
    for (Tuple t : rows) {
      int session = t.get(CONCERT_DATE.session);
      dateMap.putIfAbsent(session, new ConcertDateInfoResponse(
          t.get(CONCERT_DATE.performanceDate),
          session
      ));
    }
    concertInfoResponse.setConcertDateInfoResponseList(
        dateMap.values().stream()
            .sorted(Comparator.comparing(ConcertDateInfoResponse::getSession))
            .toList()
    );

    // —— 티켓 오픈일 중복 제거 (key: ticketOpenType) —— //
    Map<TicketOpenType, TicketOpenDateInfoResponse> openMap = new LinkedHashMap<>();
    for (Tuple t : rows) {
      LocalDateTime openDate = t.get(TICKET_OPEN_DATE.openDate);
      if (openDate != null && openDate.isBefore(now)) {
        continue;
      }
      TicketOpenType type = t.get(TICKET_OPEN_DATE.ticketOpenType);
      openMap.putIfAbsent(type, new TicketOpenDateInfoResponse(
          t.get(TICKET_OPEN_DATE.openDate),
          t.get(TICKET_OPEN_DATE.requestMaxCount),
          t.get(TICKET_OPEN_DATE.isBankTransfer),
          type
      ));
    }
    concertInfoResponse.setTicketOpenDateInfoResponseList(
        openMap.values().stream()
            .sorted(Comparator.comparing(TicketOpenDateInfoResponse::getOpenDate))
            .toList()
    );

    return concertInfoResponse;
  }

  @Override
  public ConcertInfoResponse findConcertInfoResponseByConcertIdForAdmin(UUID concertId) {

    List<Tuple> rows = queryFactory
        .select(
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertThumbnailUrl,
            CONCERT.seatingChartUrl,
            CONCERT.concertType,
            TICKET_OPEN_DATE.openDate,
            TICKET_OPEN_DATE.requestMaxCount,
            TICKET_OPEN_DATE.isBankTransfer,
            TICKET_OPEN_DATE.ticketOpenType,
            CONCERT_DATE.performanceDate,
            CONCERT_DATE.session,
            CONCERT.ticketReservationSite
        )
        .from(CONCERT)
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        .leftJoin(CONCERT_DATE).on(CONCERT_DATE.concert.eq(CONCERT))
        .leftJoin(TICKET_OPEN_DATE).on(TICKET_OPEN_DATE.concert.eq(CONCERT))
        .where(CONCERT.concertId.eq(concertId))
        .fetch();
    if (rows.isEmpty()) {
      throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    // 1) 공통 ConcertInfo 필드 추출 (rows 가 비어있지 않다고 가정)
    Tuple firstRow = rows.get(0);
    ConcertInfoResponse concertInfoResponse = ConcertInfoResponse.builder()
        .concertName(firstRow.get(CONCERT.concertName))
        .concertHallName(firstRow.get(CONCERT_HALL.concertHallName))
        .concertThumbnailUrl(firstRow.get(CONCERT.concertThumbnailUrl))
        .seatingChartUrl(firstRow.get(CONCERT.seatingChartUrl))
        .concertType(firstRow.get(CONCERT.concertType))
        .ticketReservationSite(firstRow.get(CONCERT.ticketReservationSite))
        .build();

    // —— 공연 일자 중복 제거 (key: session) —— //
    Map<Integer, ConcertDateInfoResponse> dateMap = new LinkedHashMap<>();
    for (Tuple t : rows) {
      int session = t.get(CONCERT_DATE.session);
      dateMap.putIfAbsent(session, new ConcertDateInfoResponse(
          t.get(CONCERT_DATE.performanceDate),
          session
      ));
    }
    concertInfoResponse.setConcertDateInfoResponseList(
        dateMap.values().stream()
            .sorted(Comparator.comparing(ConcertDateInfoResponse::getSession))
            .toList()
    );

    // —— 티켓 오픈일 중복 제거 (key: ticketOpenType) —— //
    Map<TicketOpenType, TicketOpenDateInfoResponse> openMap = new LinkedHashMap<>();
    for (Tuple t : rows) {
      TicketOpenType type = t.get(TICKET_OPEN_DATE.ticketOpenType);
      openMap.putIfAbsent(type, new TicketOpenDateInfoResponse(
          t.get(TICKET_OPEN_DATE.openDate),
          t.get(TICKET_OPEN_DATE.requestMaxCount),
          t.get(TICKET_OPEN_DATE.isBankTransfer),
          type
      ));
    }
    concertInfoResponse.setTicketOpenDateInfoResponseList(
        openMap.values().stream()
            .sorted(Comparator.comparing(TicketOpenDateInfoResponse::getOpenDate))
            .toList()
    );

    return concertInfoResponse;
  }

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

    LocalDateTime now = LocalDateTime.now(zone);

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(CONCERT.concertName, concertName),
        QueryDslUtil.likeIgnoreCase(CONCERT_HALL.concertHallName, concertHallName),
        QueryDslUtil.eqIfNotNull(CONCERT.concertType, concertType),
        QueryDslUtil.eqIfNotNull(CONCERT.ticketReservationSite, ticketReservationSite)
    );

    // CASE WHEN 로직 추출
    DateTimeExpression<LocalDateTime> preOpenDateExpression = buildOpenDateExpression(
        TICKET_OPEN_DATE, TicketOpenType.PRE_OPEN, now
    );
    DateTimeExpression<LocalDateTime> generalOpenDateExpression = buildOpenDateExpression(
        TICKET_OPEN_DATE, TicketOpenType.GENERAL_OPEN, now
    );
    ComparableExpression<Boolean> preOpenBankTransfer = buildBackTransferExpression(
        TICKET_OPEN_DATE, TicketOpenType.PRE_OPEN, now
    );
    ComparableExpression<Boolean> generalOpenBankTransfer = buildBackTransferExpression(
        TICKET_OPEN_DATE, TicketOpenType.GENERAL_OPEN, now
    );

    // 쿼리 작성 (JOIN + GROUP BY + CASE WHEN)
    JPAQuery<ConcertFilteredResponse> contentQuery = queryFactory
        .select(Projections.constructor(ConcertFilteredResponse.class,
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertType,
            CONCERT.ticketReservationSite,

            // 성능 최적화를 위한 CASE WHEN
            // 선예매 오픈일
            preOpenDateExpression.as("ticketPreOpenDate"),
            // 선예매 무통장 여부
            Expressions.booleanTemplate("bool_or({0})", preOpenBankTransfer).as("preOpenBankTransfer"),
            // 일반 예매 오픈일
            generalOpenDateExpression.as("ticketGeneralOpenDate"),
            // 일반 예매 무통장 여부
            Expressions.booleanTemplate("bool_or({0})", generalOpenBankTransfer).as("generalOpenBankTransfer"),

            CONCERT_DATE.performanceDate.min().as("startDate"), // 공연 시작일
            CONCERT_DATE.performanceDate.max().as("endDate"), // 공연 종료일
            CONCERT.concertThumbnailUrl,
            CONCERT.seatingChartUrl
        ))
        .from(CONCERT)
        // 공연장은 필수가 아니므로 Left Join
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        // 공연일(ConcertDate), 티켓오픈일(TicketOpenDate)은 필수이므로 Inner Join
        .join(CONCERT_DATE).on(CONCERT.concertId.eq(CONCERT_DATE.concert.concertId))
        .join(TICKET_OPEN_DATE).on(CONCERT.concertId.eq(TICKET_OPEN_DATE.concert.concertId))
        .where(whereClause)
        .groupBy(CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertType,
            CONCERT.ticketReservationSite,
            CONCERT.concertThumbnailUrl,
            CONCERT.seatingChartUrl
        )
        .having(
            preOpenDateExpression.isNotNull()
                .or(generalOpenDateExpression.isNotNull()
                )
        );

    ComparableExpression<LocalDateTime> earliestOpenDateExpression = Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "least({0}, {1})",
        preOpenDateExpression,
        generalOpenDateExpression
    );

    // enum.property -> 표현식 매핑
    Map<String, ComparableExpression<?>> customSortMap = Collections.singletonMap(
        ConcertSortField.TICKET_OPEN_DATE.getProperty(),
        earliestOpenDateExpression
    );

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        Concert.class,
        CONCERT.getMetadata().getName(),
        customSortMap
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(CONCERT.count())
        .from(CONCERT)
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        .where(whereClause);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }

  /**
   * 관리자 공연 필터링 조회
   * 티켓 오픈일이 지난 공연도 반환합니다
   *
   * @param concertName           공연 이름 (검색어)
   * @param concertHallName       공연장 이름 (검색어)
   * @param concertType           공연 카테고리
   * @param ticketReservationSite 예매처
   * @return concertFilteredResponse
   */
  @Override
  public Page<ConcertFilteredResponse> filteredConcertForAdmin(
      String concertName,
      String concertHallName,
      ConcertType concertType,
      TicketReservationSite ticketReservationSite,
      Pageable pageable) {

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.likeIgnoreCase(CONCERT.concertName, concertName),
        QueryDslUtil.likeIgnoreCase(CONCERT_HALL.concertHallName, concertHallName),
        QueryDslUtil.eqIfNotNull(CONCERT.concertType, concertType),
        QueryDslUtil.eqIfNotNull(CONCERT.ticketReservationSite, ticketReservationSite)
    );

    ComparableExpression<LocalDateTime> preOpenDateExpression = Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "min({0})",
        new CaseBuilder()
            .when(TICKET_OPEN_DATE.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
            .then(TICKET_OPEN_DATE.openDate)
            .otherwise((LocalDateTime) null)
    ).as("ticketPreOpenDate");

    ComparableExpression<LocalDateTime> generalOpenDateExpression = Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "min({0})",
        new CaseBuilder()
            .when(TICKET_OPEN_DATE.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
            .then(TICKET_OPEN_DATE.openDate)
            .otherwise((LocalDateTime) null)
    ).as("ticketGeneralOpenDate");

    // 쿼리 작성 (JOIN + GROUP BY + CASE WHEN)
    JPAQuery<ConcertFilteredResponse> contentQuery = queryFactory
        .select(Projections.constructor(ConcertFilteredResponse.class,
            CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertType,
            CONCERT.ticketReservationSite,

            // 성능 최적화를 위한 CASE WHEN
            // 선예매 오픈일
            preOpenDateExpression,
            // 선예매 무통장 여부
            Expressions.booleanTemplate(
                "bool_or({0})",
                new CaseBuilder()
                    .when(TICKET_OPEN_DATE.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
                    .then((ComparableExpression<Boolean>) TICKET_OPEN_DATE.isBankTransfer)
                    .otherwise((Boolean) null)
            ).as("preOpenBankTransfer"),
            // 일반 예매 오픈일
            generalOpenDateExpression,
            // 일반 예매 무통장 여부
            Expressions.booleanTemplate(
                "bool_or({0})",
                new CaseBuilder()
                    .when(TICKET_OPEN_DATE.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
                    .then((ComparableExpression<Boolean>) TICKET_OPEN_DATE.isBankTransfer)
                    .otherwise((Boolean) null)
            ).as("generalOpenBankTransfer"),
            CONCERT_DATE.performanceDate.min().as("startDate"), // 공연 시작일
            CONCERT_DATE.performanceDate.max().as("endDate"), // 공연 종료일
            CONCERT.concertThumbnailUrl,
            CONCERT.seatingChartUrl
        ))
        .from(CONCERT)
        // 공연장은 필수가 아니므로 Left Join
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        // 공연일(ConcertDate), 티켓오픈일(TicketOpenDate)은 필수이므로 Inner Join
        .join(CONCERT_DATE).on(CONCERT.concertId.eq(CONCERT_DATE.concert.concertId))
        .join(TICKET_OPEN_DATE).on(CONCERT.concertId.eq(TICKET_OPEN_DATE.concert.concertId))
        .where(whereClause)
        .groupBy(CONCERT.concertId,
            CONCERT.concertName,
            CONCERT_HALL.concertHallName,
            CONCERT.concertType,
            CONCERT.ticketReservationSite,
            CONCERT.concertThumbnailUrl,
            CONCERT.seatingChartUrl
        );

    ComparableExpression<LocalDateTime> earliestOpenDateExpression = Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "least({0}, {1})",
        preOpenDateExpression,
        generalOpenDateExpression
    );

    // enum.property -> 표현식 매핑
    Map<String, ComparableExpression<?>> customSortMap = Collections.singletonMap(
        ConcertSortField.TICKET_OPEN_DATE.getProperty(),
        earliestOpenDateExpression
    );

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        Concert.class,
        CONCERT.getMetadata().getName(),
        customSortMap
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(CONCERT.count())
        .from(CONCERT)
        .leftJoin(CONCERT.concertHall, CONCERT_HALL)
        .where(whereClause);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }

  /**
   * 지정된 티켓 오픈 타입과 현재 시각을 기준으로 유효한 openDate를 집계하는 표현식을 생성합니다.
   *
   * @param ticketOpenDate QTicketOpenDate 엔티티
   * @param ticketOpenType 대상 TicketOpenType (PRE_OPEN 또는 GENERAL_OPEN)
   * @param now            현재 기준 시각
   * @return 지정한 openType의 openDate 중 now 이후인 값의 최소값을 반환하는 DateTimeExpression
   */
  private DateTimeExpression<LocalDateTime> buildOpenDateExpression(
      QTicketOpenDate ticketOpenDate,
      TicketOpenType ticketOpenType,
      LocalDateTime now
  ) {
    return Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "min({0})",
        new CaseBuilder()
            .when(ticketOpenDate.ticketOpenType.eq(ticketOpenType)
                .and(ticketOpenDate.openDate.gt(Expressions.constant(now))))
            .then(ticketOpenDate.openDate)
            .otherwise((LocalDateTime) null)
    );
  }

  /**
   * 지정된 티켓 오픈 타입에 대해 유효한 bank transfer 여부 필드를 CASE WHEN 비교 후 반환하는 표현식을 생성합니다.
   *
   * @param ticketOpenDate QTicketOpenDate 엔티티
   * @param ticketOpenType 대상 TicketOpenType (PRE_OPEN 또는 GENERAL_OPEN)
   * @param now            현재 기준 시각
   * @return now 이후인 openType의 isBankTransfer 값을 담은 ComparableExpression<Boolean>
   */
  private ComparableExpression<Boolean> buildBackTransferExpression(
      QTicketOpenDate ticketOpenDate,
      TicketOpenType ticketOpenType,
      LocalDateTime now
  ) {
    return new CaseBuilder()
        .when(ticketOpenDate.ticketOpenType.eq(ticketOpenType)
            .and(ticketOpenDate.openDate.gt(Expressions.constant(now))))
        .then((ComparableExpression<Boolean>) ticketOpenDate.isBankTransfer)
        .otherwise((Boolean) null);
  }
}
