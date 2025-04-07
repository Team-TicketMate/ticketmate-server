package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketOpenDateRepository extends JpaRepository<TicketOpenDate, UUID> {

    List<TicketOpenDate> findAllByConcert_ConcertId(UUID concertId);

    Optional<TicketOpenDate> findByConcert_ConcertIdAndIsPreOpen(UUID concertId, Boolean isPreOpen);
}
