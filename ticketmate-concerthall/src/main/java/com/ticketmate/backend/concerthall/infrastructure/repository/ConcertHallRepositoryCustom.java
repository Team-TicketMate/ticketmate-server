package com.ticketmate.backend.concerthall.infrastructure.repository;

import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.concerthall.core.constant.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertHallRepositoryCustom {

  Page<ConcertHallFilteredResponse> filteredConcertHall(
      String concertHallName,
      City city,
      Pageable pageable
  );
}
