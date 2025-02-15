package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    // ConcertHall -> ConcertHallFilteredResponse
    ConcertHallFilteredResponse toConcertHallFilteredResponse(ConcertHall concertHall);

    // Concert -> ConcertFilteredResponse
    @Mapping(source = "concertHall.concertHallName", target = "concertHallName")
    ConcertFilteredResponse toConcertFilteredResponse(Concert concert);
}
