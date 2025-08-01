package com.ticketmate.backend.concerthall.application.mapper;

import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallInfoResponse;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConcertHallMapper {

  // ConcertHall -> ConcertHallFilteredResponse (엔티티 -> DTO)
  ConcertHallFilteredResponse toConcertHallFilteredResponse(ConcertHall concertHall);

  // ConcertHall -> ConcertHallInfoResponse (엔티티 -> DTO)
  ConcertHallInfoResponse toConcertHallInfoResponse(ConcertHall concertHall);
}
