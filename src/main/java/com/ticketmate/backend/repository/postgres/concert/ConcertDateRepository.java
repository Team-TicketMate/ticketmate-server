package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConcertDateRepository extends JpaRepository<ConcertDate, UUID> {

}
