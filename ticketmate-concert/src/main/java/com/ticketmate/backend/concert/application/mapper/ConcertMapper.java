package com.ticketmate.backend.concert.application.mapper;

import com.ticketmate.backend.concert.application.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.concert.application.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConcertMapper {

  // List<ConcertDate> -> List<ConcertDateInfoResponse> (엔티티 리스트 -> DTO 리스트)
  List<ConcertDateInfoResponse> toConcertDateInfoResponseList(List<ConcertDate> concertDateList);

  // List<TicketOpenDate> -> List<TicketOpenDateInfoResponse> (엔티티 리스트 -> DTO 리스트)
  List<TicketOpenDateInfoResponse> toTicketOpenDateInfoResponseList(List<TicketOpenDate> ticketOpenDateList);

}
