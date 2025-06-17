package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertRepositoryCustom {

  Page<ConcertFilteredResponse> filteredConcert(
      String concertName,
      String concertHallName,
      ConcertType concertType,
      TicketReservationSite ticketReservationSite,
      Pageable pageable
  );
}
