package com.ticketmate.backend.concert.application.dto.view;

import com.ticketmate.backend.concert.application.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.concert.application.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.util.List;

public record ConcertInfo(
    String concertName,
    String concertHallName,
    String concertThumbnailStoredPath,
    String seatingChartStoredPath,
    ConcertType concertType,
    List<ConcertDateInfoResponse> concertDateInfoResponseList,
    List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList,
    TicketReservationSite ticketReservationSite
) {

}
