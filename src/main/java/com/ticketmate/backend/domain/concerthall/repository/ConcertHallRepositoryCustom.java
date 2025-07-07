package com.ticketmate.backend.domain.concerthall.repository;

import com.ticketmate.backend.domain.concerthall.domain.constant.City;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertHallRepositoryCustom {

  Page<ConcertHallFilteredResponse> filteredConcertHall(
      String concertHallName,
      City city,
      Pageable pageable
  );
}
