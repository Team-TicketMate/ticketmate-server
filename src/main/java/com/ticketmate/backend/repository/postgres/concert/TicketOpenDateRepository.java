package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketOpenDateRepository extends JpaRepository<TicketOpenDate, UUID> {
}
