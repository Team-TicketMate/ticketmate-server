package com.ticketmate.backend.domain.concert.repository;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketOpenDateRepository extends JpaRepository<TicketOpenDate, UUID> {

  List<TicketOpenDate> findAllByConcertConcertId(UUID concertId);

  Optional<TicketOpenDate> findByConcertConcertIdAndTicketOpenType(UUID concertId, TicketOpenType ticketOpenType);

  void deleteAllByConcertConcertId(UUID concertId);
}
