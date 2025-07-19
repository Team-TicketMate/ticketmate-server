package com.ticketmate.backend.domain.concert.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.concert.domain.constant.ConcertSortField;
import com.ticketmate.backend.domain.concert.domain.constant.ConcertType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketReservationSite;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.QConcert;
import com.ticketmate.backend.domain.concert.domain.entity.QConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.QTicketOpenDate;
import com.ticketmate.backend.domain.concerthall.domain.entity.QConcertHall;
import com.ticketmate.backend.domain.search.domain.dto.response.ConcertSearchResponse;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
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

  private final JPAQueryFactory queryFactory;
  private final ZoneId zone = ZoneId.of("Asia/Seoul");

  private final QConcert concert = QConcert.concert;
  private final QConcertHall concertHall = QConcertHall.concertHall;
  private final QConcertDate concertDate = QConcertDate.concertDate;
  private final QTicketOpenDate ticketOpenDate = QTicketOpenDate.ticketOpenDate;

  /**
   * 공연 상세 조회
   */
  @Override
  public ConcertInfoResponse findConcertInfoResponseByConcertId(UUID concertId) {

    LocalDateTime now = LocalDateTime.now(zone);

    List<Tuple> rows = queryFactory
        .select(
            concert.concertId,
            concert.concertName,
            concertHall.concertHallName,
            concert.concertThumbnailUrl,
            concert.seatingChartUrl,
            concert.concertType,
            ticketOpenDate.openDate,
            ticketOpenDate.requestMaxCount,
            ticketOpenDate.isBankTransfer,
            ticketOpenDate.ticketOpenType,
            concertDate.performanceDate,
            concertDate.session,
            concert.ticketReservationSite
        )
        .from(concert)
        .leftJoin(concert.concertHall, concertHall)
        .leftJoin(concertDate).on(concertDate.concert.eq(concert))
        .leftJoin(ticketOpenDate).on(ticketOpenDate.concert.eq(concert))
        .where(concert.concertId.eq(concertId))
        .fetch();
    if (rows.isEmpty()) {
      throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    // 1) 공통 ConcertInfo 필드 추출 (rows 가 비어있지 않다고 가정)
    Tuple firstRow = rows.get(0);
    ConcertInfoResponse concertInfoResponse = ConcertInfoResponse.builder()
        .concertName(firstRow.get(concert.concertName))
        .concertHallName(firstRow.get(concertHall.concertHallName))
        .concertThumbnailUrl(firstRow.get(concert.concertThumbnailUrl))
        .seatingChartUrl(firstRow.get(concert.seatingChartUrl))
        .concertType(firstRow.get(concert.concertType))
        .ticketReservationSite(firstRow.get(concert.ticketReservationSite))
        .build();

    // —— 공연 일자 중복 제거 (key: session) —— //
    Map<Integer, ConcertDateInfoResponse> dateMap = new LinkedHashMap<>();
    for (Tuple t : rows) {
      int session = t.get(concertDate.session);
      dateMap.putIfAbsent(session, new ConcertDateInfoResponse(
          t.get(concertDate.performanceDate),
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
      LocalDateTime openDate = t.get(ticketOpenDate.openDate);
      if (openDate != null && openDate.isBefore(now)) {
        continue;
      }
      TicketOpenType type = t.get(ticketOpenDate.ticketOpenType);
      openMap.putIfAbsent(type, new TicketOpenDateInfoResponse(
          t.get(ticketOpenDate.openDate),
          t.get(ticketOpenDate.requestMaxCount),
          t.get(ticketOpenDate.isBankTransfer),
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
            concert.concertId,
            concert.concertName,
            concertHall.concertHallName,
            concert.concertThumbnailUrl,
            concert.seatingChartUrl,
            concert.concertType,
            ticketOpenDate.openDate,
            ticketOpenDate.requestMaxCount,
            ticketOpenDate.isBankTransfer,
            ticketOpenDate.ticketOpenType,
            concertDate.performanceDate,
            concertDate.session,
            concert.ticketReservationSite
        )
        .from(concert)
        .leftJoin(concert.concertHall, concertHall)
        .leftJoin(concertDate).on(concertDate.concert.eq(concert))
        .leftJoin(ticketOpenDate).on(ticketOpenDate.concert.eq(concert))
        .where(concert.concertId.eq(concertId))
        .fetch();
    if (rows.isEmpty()) {
      throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    // 1) 공통 ConcertInfo 필드 추출 (rows 가 비어있지 않다고 가정)
    Tuple firstRow = rows.get(0);
    ConcertInfoResponse concertInfoResponse = ConcertInfoResponse.builder()
        .concertName(firstRow.get(concert.concertName))
        .concertHallName(firstRow.get(concertHall.concertHallName))
        .concertThumbnailUrl(firstRow.get(concert.concertThumbnailUrl))
        .seatingChartUrl(firstRow.get(concert.seatingChartUrl))
        .concertType(firstRow.get(concert.concertType))
        .ticketReservationSite(firstRow.get(concert.ticketReservationSite))
        .build();

    // —— 공연 일자 중복 제거 (key: session) —— //
    Map<Integer, ConcertDateInfoResponse> dateMap = new LinkedHashMap<>();
    for (Tuple t : rows) {
      int session = t.get(concertDate.session);
      dateMap.putIfAbsent(session, new ConcertDateInfoResponse(
          t.get(concertDate.performanceDate),
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
      TicketOpenType type = t.get(ticketOpenDate.ticketOpenType);
      openMap.putIfAbsent(type, new TicketOpenDateInfoResponse(
          t.get(ticketOpenDate.openDate),
          t.get(ticketOpenDate.requestMaxCount),
          t.get(ticketOpenDate.isBankTransfer),
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
        QueryDslUtil.likeIgnoreCase(concert.concertName, concertName),
        QueryDslUtil.likeIgnoreCase(concertHall.concertHallName, concertHallName),
        QueryDslUtil.eqIfNotNull(concert.concertType, concertType),
        QueryDslUtil.eqIfNotNull(concert.ticketReservationSite, ticketReservationSite)
    );

    // CASE WHEN 로직 추출
    DateTimeExpression<LocalDateTime> preOpenDateExpression = buildOpenDateExpression(
        ticketOpenDate, TicketOpenType.PRE_OPEN, now
    );
    DateTimeExpression<LocalDateTime> generalOpenDateExpression = buildOpenDateExpression(
        ticketOpenDate, TicketOpenType.GENERAL_OPEN, now
    );
    ComparableExpression<Boolean> preOpenBankTransfer = buildBackTransferExpression(
        ticketOpenDate, TicketOpenType.PRE_OPEN, now
    );
    ComparableExpression<Boolean> generalOpenBankTransfer = buildBackTransferExpression(
        ticketOpenDate, TicketOpenType.GENERAL_OPEN, now
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
            preOpenDateExpression.as("ticketPreOpenDate"),
            // 선예매 무통장 여부
            Expressions.booleanTemplate("bool_or({0})", preOpenBankTransfer).as("preOpenBankTransfer"),
            // 일반 예매 오픈일
            generalOpenDateExpression.as("ticketGeneralOpenDate"),
            // 일반 예매 무통장 여부
            Expressions.booleanTemplate("bool_or({0})", generalOpenBankTransfer).as("generalOpenBankTransfer"),

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
        concert.getMetadata().getName(),
        customSortMap
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(concert.count())
        .from(concert)
        .leftJoin(concert.concertHall, concertHall)
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
        QueryDslUtil.likeIgnoreCase(concert.concertName, concertName),
        QueryDslUtil.likeIgnoreCase(concertHall.concertHallName, concertHallName),
        QueryDslUtil.eqIfNotNull(concert.concertType, concertType),
        QueryDslUtil.eqIfNotNull(concert.ticketReservationSite, ticketReservationSite)
    );

    ComparableExpression<LocalDateTime> preOpenDateExpression = Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "min({0})",
        new CaseBuilder()
            .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
            .then(ticketOpenDate.openDate)
            .otherwise((LocalDateTime) null)
    ).as("ticketPreOpenDate");

    ComparableExpression<LocalDateTime> generalOpenDateExpression = Expressions.dateTimeTemplate(
        LocalDateTime.class,
        "min({0})",
        new CaseBuilder()
            .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
            .then(ticketOpenDate.openDate)
            .otherwise((LocalDateTime) null)
    ).as("ticketGeneralOpenDate");

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
            preOpenDateExpression,
            // 선예매 무통장 여부
            Expressions.booleanTemplate(
                "bool_or({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
                    .then((ComparableExpression<Boolean>) ticketOpenDate.isBankTransfer)
                    .otherwise((Boolean) null)
            ).as("preOpenBankTransfer"),
            // 일반 예매 오픈일
            generalOpenDateExpression,
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
        concert.getMetadata().getName(),
        customSortMap
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(concert.count())
        .from(concert)
        .leftJoin(concert.concertHall, concertHall)
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

  /**
   * 키워드를 사용하여 공연명, 공연장명, 공연 타입에 대해 LIKE 검색을 수행하고 일치하는 공연 ID 목록을 반환합니다.
   *
   * @param keyword 검색할 키워드
   * @return 일치하는 공연의 UUID 리스트
   */
  @Override
  public List<UUID> findConcertIdsByKeyword(String keyword){
    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.anyOf(
        QueryDslUtil.likeIgnoreCase(concert.concertName, keyword),
        QueryDslUtil.likeIgnoreCase(concertHall.concertHallName, keyword),
        QueryDslUtil.likeIgnoreCase(concert.concertType.stringValue(), keyword)
    );
    return queryFactory
        .select(concert.concertId)
        .from(concert)
        .where(whereClause)
        .fetch();
  }

  /**
   * 제공된 ID 목록에 해당하는 공연들의 상세 정보를 조회하여 ConcertSearchResponse DTO 리스트로 반환합니다.
   *
   * @param concertIds 상세 정보를 조회할 공연의 UUID 리스트
   * @return 공연 상세 정보가 담긴 DTO 리스트
   */
  @Override
  public List<ConcertSearchResponse> findConcertDetailsByIds(List<UUID> concertIds) {
    if (concertIds == null || concertIds.isEmpty()) {
      return Collections.emptyList();
    }
    return queryFactory
        .select(Projections.constructor(ConcertSearchResponse.class,
            concert.concertId,
            concert.concertName,
            concertHall.concertHallName,
            // 선예매 오픈일
            Expressions.dateTimeTemplate(
                LocalDateTime.class,
                "min({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.PRE_OPEN))
                    .then(ticketOpenDate.openDate)
                    .otherwise((LocalDateTime) null)
            ).as("ticketPreOpenDate"),
            // 일반 예매 오픈일
            Expressions.dateTimeTemplate(
                LocalDateTime.class,
                "min({0})",
                new CaseBuilder()
                    .when(ticketOpenDate.ticketOpenType.eq(TicketOpenType.GENERAL_OPEN))
                    .then(ticketOpenDate.openDate)
                    .otherwise((LocalDateTime) null)
            ).as("ticketGeneralOpenDate"),
            concertDate.performanceDate.min().as("startDate"),
            concertDate.performanceDate.max().as("endDate"),
            concert.concertThumbnailUrl,
            Expressions.constant(0.0)
        ))
        .from(concert)
        .leftJoin(concert.concertHall, concertHall)
        .join(concertDate).on(concert.eq(concertDate.concert))
        .join(ticketOpenDate).on(concert.eq(ticketOpenDate.concert))
        .where(concert.concertId.in(concertIds))
        .groupBy(concert.concertId,
            concert.concertName,
            concertHall.concertHallName,
            concert.concertThumbnailUrl
        )
        .fetch();
  }
}
