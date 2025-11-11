package com.ticketmate.backend.concert.application.mapper;

import com.ticketmate.backend.concert.application.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.concert.application.dto.view.ConcertDateInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertFilteredInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertInfo;
import com.ticketmate.backend.concert.application.dto.view.TicketOpenDateInfo;

public interface ConcertMapper {

  /**
   * ConcertInfo -> ConcertInfoResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   * Instant -> LocalDateTime 변환
   */
  ConcertInfoResponse toConcertInfoResponse(ConcertInfo info);

  /**
   * ConcertFilteredInfo -> ConcertFilteredResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertFilteredResponse toConcertFilteredResponse(ConcertFilteredInfo info);

  /**
   * ConcertDateInfo -> ConcertDateInfoResponse (DTO -> DTO)
   * Instant -> LocalDateTime 변환
   */
  ConcertDateInfoResponse toConcertDateInfoResponse(ConcertDateInfo info);

  /**
   * TicketOpenDateInfo -> TicketOpenDateInfoResponse (DTO -> DTO)
   * Instant -> LocalDateTime 변환
   */
  TicketOpenDateInfoResponse toTicketOpenDateInfoResponse(TicketOpenDateInfo info);
}
