package com.ticketmate.backend.domain.concert.service;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concert.repository.ConcertDateRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertRepositoryCustom;
import com.ticketmate.backend.domain.concert.repository.TicketOpenDateRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

  private final ConcertRepository concertRepository;
  private final ConcertDateRepository concertDateRepository;
  private final TicketOpenDateRepository ticketOpenDateRepository;
  private final ConcertRepositoryCustom concertRepositoryCustom;

  /**
   * 공연 필터링 조회 로직
   *
   * @param request concertName
   *                concertHallName
   *                concertType
   *                ticketReservationSite
   *                pageNumber (1부터 시작)
   *                pageSize
   *                sortField (ConcertSortField.java)
   *                sortDirection
   */
  @Transactional(readOnly = true)
  public Page<ConcertFilteredResponse> filteredConcert(ConcertFilteredRequest request) {
    return concertRepositoryCustom.filteredConcert(
        request.getConcertName(),
        request.getConcertHallName(),
        request.getConcertType(),
        request.getTicketReservationSite(),
        request.toPageable()
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
    return ConcertInfoResponse.of(
        findConcertById(concertId),
        concertDateRepository.findAllByConcertConcertIdOrderByPerformanceDateAsc(concertId),
        ticketOpenDateRepository.findAllByConcertConcertIdAndOpenDateAfter(concertId, LocalDateTime.now())
    );
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
