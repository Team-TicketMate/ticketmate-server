package com.ticketmate.backend.concert.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.concert.application.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.dto.view.ConcertFilteredInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertInfo;
import com.ticketmate.backend.concert.application.mapper.ConcertMapper;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertDateRepository;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepository;
import com.ticketmate.backend.concert.infrastructure.repository.ConcertRepositoryCustom;
import com.ticketmate.backend.concert.infrastructure.repository.TicketOpenDateRepository;
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
  private final ConcertMapper concertMapper;

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
    Page<ConcertFilteredInfo> concertFilteredInfoPage = concertRepositoryCustom.filteredConcert(
        request.getConcertName(),
        request.getConcertHallName(),
        request.getConcertType(),
        request.getTicketReservationSite(),
        request.toPageable()
    );
    return concertMapper.toConcertFilteredResponsePage(concertFilteredInfoPage);
  }

  /**
   * 공연 상세 조회 로직
   *
   * @param concertId 공연 PK
   * @return 공연 상세 정보
   */
  @Transactional(readOnly = true)
  public ConcertInfoResponse getConcertInfo(UUID concertId) {
    ConcertInfo concertInfo = concertRepositoryCustom.findConcertInfoByConcertId(concertId);
    return concertMapper.toConcertInfoResponse(concertInfo);
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
