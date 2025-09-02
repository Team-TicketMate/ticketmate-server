package com.ticketmate.backend.concert.infrastructure.repository;

import com.ticketmate.backend.concert.application.dto.view.ConcertFilteredInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertInfo;
import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertRepositoryCustom {

  /**
   * 공연 상세 조회
   * Concert, ConcertDate 리스트, TicketOpenDate 리스트를 한 번의 쿼리로 묶어서 ConcertInfoResposne DTO로 반환
   * 티켓 오픈일이 지난 공연 반환 X
   *
   * @param concertId 공연 PK
   */
  ConcertInfo findConcertInfoByConcertId(UUID concertId);

  /**
   * 관리자 공연 상세 조회
   * 모든 공연 정보 반환
   *
   * @param concertId 공연 PK
   */
  ConcertInfo findConcertInfoByConcertIdForAdmin(UUID concertId);

  /**
   * 공연 필터링 조회 (사용자)
   * 티켓 오픈일이 지난 공연 반환 X
   */
  Page<ConcertFilteredInfo> filteredConcert(
      String concertName,
      String concertHallName,
      ConcertType concertType,
      TicketReservationSite ticketReservationSite,
      Pageable pageable
  );

  /**
   * 공연 필터링 조회 (관리자)
   * 모든 공연 반환
   */
  Page<ConcertFilteredInfo> filteredConcertForAdmin(
      String concertName,
      String concertHallName,
      ConcertType concertType,
      TicketReservationSite ticketReservationSite,
      Pageable pageable
  );
}
