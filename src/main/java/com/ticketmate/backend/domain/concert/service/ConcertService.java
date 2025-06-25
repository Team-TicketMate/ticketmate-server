package com.ticketmate.backend.domain.concert.service;

import static com.ticketmate.backend.global.util.common.CommonUtil.nvl;

import com.ticketmate.backend.domain.concert.domain.constant.ConcertType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketReservationSite;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concert.repository.ConcertDateRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepositoryImpl;
import com.ticketmate.backend.domain.concert.repository.TicketOpenDateRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.mapper.EntityMapper;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.util.common.PageableUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

  private final ConcertRepository concertRepository;
  private final ConcertDateRepository concertDateRepository;
  private final TicketOpenDateRepository ticketOpenDateRepository;
  private final ConcertRepositoryImpl concertRepositoryImpl;
  private final EntityMapper entityMapper;

  /**
   * 공연 필터링 조회 로직
   *
   * @param request concertName
   *                concertHallName
   *                concertType
   *                ticketReservationSite
   *                pageNumber (1부터 시작)
   *                pageSize
   *                sortField
   *                sortDirection
   */
  @Transactional(readOnly = true)
  public Page<ConcertFilteredResponse> filteredConcert(ConcertFilteredRequest request) {

    // 1. 요청 값 확인
    String concertName = nvl(request.getConcertName(), "");
    String concertHallName = nvl(request.getConcertHallName(), "");
    ConcertType concertType = request.getConcertType() != null ? request.getConcertType() : null;
    TicketReservationSite ticketReservationSite = request.getTicketReservationSite() != null ? request.getTicketReservationSite() : null;

    // 2. Pageable 생성 (PageableUtil 사용)
    Pageable pageable = PageableUtil.createPageable(
        request.getPageNumber(),
        request.getPageSize(),
        request.getSortField(),
        request.getSortDirection(),
        "created_date", "ticket_open_date"
    );

    // 3. 데이터베이스 조회
    return concertRepositoryImpl.filteredConcert(
        concertName,
        concertHallName,
        concertType,
        ticketReservationSite,
        pageable
    );
  }

  /**
   * 공연 상세 조회 로직
   *
   * @param concertId 공연 PK
   * @return 공연 상세 정보
   */
  @Transactional(readOnly = true)
  public ConcertInfoResponse getConcertInfo(UUID concertId) {

    // DB에서 공연정보, 공연일자, 티켓오픈일 조회
    Concert concert = concertRepository.findById(concertId)
        .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

    // 공연장 이름 확인
    String concertHallName = null;
    if (concert.getConcertHall() != null) { // 공연장 정보가 존재하는 경우
      concertHallName = concert.getConcertHall().getConcertHallName();
    }

    // 예매처 확인
    TicketReservationSite ticketReservationSite = null;
    if (concert.getTicketReservationSite() != null) {
      ticketReservationSite = concert.getTicketReservationSite();
    }

    // 좌석배치도 URL 확인
    String seatingChartUrl = null;
    if (concert.getSeatingChartUrl() != null) {
      seatingChartUrl = concert.getSeatingChartUrl();
    }

    // 비동기적으로 관련 데이터 멀티스레드 조회
    CompletableFuture<List<ConcertDate>> concertDateListFuture = CompletableFuture
        .supplyAsync(() -> concertDateRepository.findAllByConcertConcertId(concert.getConcertId()));
    CompletableFuture<List<TicketOpenDate>> ticketOpenDateListFuture = CompletableFuture
        .supplyAsync(() -> ticketOpenDateRepository.findAllByConcertConcertId(concert.getConcertId()));

    // 데이터 조회 완료 대기
    List<ConcertDate> concertDateList;
    List<TicketOpenDate> ticketOpenDateList;
    try {
      concertDateList = concertDateListFuture.get();
      ticketOpenDateList = ticketOpenDateListFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("공연 관련 데이터 멀티스레드 조회 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // 공연날짜 DTO List 생성
    List<ConcertDateInfoResponse> concertDateInfoResponseList = entityMapper.toConcertDateInfoResponseList(concertDateList);

    // 티켓 오픈일 검증
    validateTicketOpenDateList(ticketOpenDateList);

    // 티켓 오픈일 DTO List 생성
    List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList = entityMapper.toTicketOpenDateInfoResponseList(ticketOpenDateList);

    // 4. 반환값
    return ConcertInfoResponse.builder()
        .concertName(concert.getConcertName())
        .concertHallName(concertHallName)
        .concertThumbnailUrl(concert.getConcertThumbnailUrl())
        .seatingChartUrl(seatingChartUrl)
        .concertType(concert.getConcertType())
        .concertDateInfoResponseList(concertDateInfoResponseList)
        .ticketOpenDateInfoResponses(ticketOpenDateInfoResponseList)
        .ticketReservationSite(ticketReservationSite)
        .build();
  }

  /**
   * 티켓 오픈일 검증
   * 1. 선예매/일반예매 모두 없는경우
   * 2. 선예매/일반예매가 각각 한개를 초과하여 등록된 경우
   */
  private void validateTicketOpenDateList(List<TicketOpenDate> ticketOpenDateList) {
    // Enum 키를 위한 EnumMap 사용
    Map<TicketOpenType, List<TicketOpenDate>> openDateListByType = new EnumMap<>(TicketOpenType.class);

    // 티켓 오픈일 분류
    for (TicketOpenDate date : ticketOpenDateList) {
      TicketOpenType type = date.getTicketOpenType();
      // TicketOpenType 검증
      if (type != TicketOpenType.PRE_OPEN && type != TicketOpenType.GENERAL_OPEN) {
        log.error("TicketOpenDate 객체 내부에 잘못된 TicketOpenType이 존재합니다: {}", type);
        throw new CustomException(ErrorCode.TICKET_OPEN_TYPE_NOT_FOUND);
      }
      openDateListByType.computeIfAbsent(type, ticketOpenType -> new ArrayList<>()).add(date);
    }

    // 검증을 위한 리스트 추출
    List<TicketOpenDate> preOpenDateList = openDateListByType.getOrDefault(TicketOpenType.PRE_OPEN, Collections.emptyList());
    List<TicketOpenDate> generalOpenDateList = openDateListByType.getOrDefault(TicketOpenType.GENERAL_OPEN, Collections.emptyList());

    // 검증
    if (CommonUtil.nullOrEmpty(preOpenDateList) && CommonUtil.nullOrEmpty(generalOpenDateList)) {
      log.error("선예매/일반예매 오픈일 데이터가 모두 비어있습니다.");
      throw new CustomException(ErrorCode.TICKET_OPEN_DATE_NOT_FOUND);
    }

    if (preOpenDateList.size() > 1) {
      log.error("선예매 오픈일이 여러 개 등록되어있습니다. 등록된 선예매 오픈일 정보 개수: {}개", preOpenDateList.size());
      throw new CustomException(ErrorCode.PRE_OPEN_COUNT_EXCEED);
    }
    if (generalOpenDateList.size() > 1) {
      log.error("일반예매 오픈일이 여러 개 등록되어있습니다. 등록된 일반예매 오픈일 정보 개수: {}개", generalOpenDateList.size());
      throw new CustomException(ErrorCode.GENERAL_OPEN_COUNT_EXCEED);
    }
  }

  /**
   * concertId에 해당하는 공연 반환
   *
   * @param concertId 공연 PK
   */
  public Concert findConcertById(UUID concertId) {
    return concertRepository.findById(concertId)
        .orElseThrow(() -> {
          log.error("요청한 PK값에 해당하는 공연을 찾을 수 없습니다. 요청 PK: {}", concertId);
          return new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        });
  }

  /**
   * 티켓 오픈일(TicketOpenDate) 조회
   *
   * @param concertId      공연PK
   * @param ticketOpenType 선예매/일반예매
   */
  public TicketOpenDate findTicketOpenDateByConcertIdAndTicketOpenType(UUID concertId, TicketOpenType ticketOpenType) {
    return ticketOpenDateRepository
        .findByConcertConcertIdAndTicketOpenType(concertId, ticketOpenType)
        .orElseThrow(() -> {
          log.error("공연: {} 에 해당하는 {} 공연 정보가 존재하지 않습니다.", concertId, ticketOpenType.getDescription());
          return new CustomException(ErrorCode.TICKET_OPEN_DATE_NOT_FOUND);
        });
  }

  /**
   * 공연일(ConcertDate) 조회
   *
   * @param concertId       공연PK
   * @param performanceDate 공연날짜
   */
  public ConcertDate findConcertDateByConcertIdAndPerformanceDate(UUID concertId, LocalDateTime performanceDate) {
    return concertDateRepository
        .findByConcertConcertIdAndPerformanceDate(concertId, performanceDate)
        .orElseThrow(() -> {
          log.error("공연: {} 공연일자: {} 에 해당하는 ConcertDate를 찾을 수 없습니다.",
              concertId, performanceDate);
          return new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND);
        });
  }
}
