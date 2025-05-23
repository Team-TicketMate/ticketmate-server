package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketOpenDateRepository extends JpaRepository<TicketOpenDate, UUID> {

    List<TicketOpenDate> findAllByConcertConcertId(UUID concertId);

    Optional<TicketOpenDate> findByConcertConcertIdAndTicketOpenType(UUID concertId, TicketOpenType ticketOpenType);

    void deleteAllByConcertConcertId(UUID concertId);
}
