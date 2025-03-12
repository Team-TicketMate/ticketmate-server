package com.ticketmate.backend.test.repository;

import com.ticketmate.backend.test.object.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
