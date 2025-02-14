package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.postgres.Concert;
import com.ticketmate.backend.object.postgres.ConcertHall;
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
