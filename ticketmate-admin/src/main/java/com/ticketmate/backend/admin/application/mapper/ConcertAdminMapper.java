package com.ticketmate.backend.admin.application.mapper;

import com.ticketmate.backend.admin.application.dto.request.ConcertDateRequest;
import com.ticketmate.backend.admin.application.dto.request.TicketOpenDateRequest;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConcertAdminMapper {

  // List<ConcertDateRequest> -> List<ConcertDate> (DTO 리스트 -> 엔티티 리스트)
  List<ConcertDate> toConcertDateList(List<ConcertDateRequest> concertDateRequestList);

  // List<TicketOpenDateList> -> List<TicketOpenDate> (DTO 리스트 -> 엔티티 리스트)
  List<TicketOpenDate> toTicketOpenDateList(List<TicketOpenDateRequest> ticketOpenDateRequestList);
}
