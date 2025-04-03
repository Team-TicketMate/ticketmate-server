package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketOpenDateRepository extends JpaRepository<TicketOpenDate, UUID> {

    List<TicketOpenDate> findAllByConcert(Concert concert);

    List<TicketOpenDate> findAllByConcertAndIsPreOpen(Concert concert, Boolean isPreOpen);
}
