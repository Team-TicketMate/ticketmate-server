package com.ticketmate.backend.concert.application.dto.view;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.util.List;

public record ConcertInfo(
    String concertName,
    String concertHallName,
    String concertThumbnailStoredPath,
    String seatingChartStoredPath,
    ConcertType concertType,
    List<ConcertDateInfo> concertDateInfoList,
    List<TicketOpenDateInfo> ticketOpenDateInfoList,
    TicketReservationSite ticketReservationSite
) {

}
