package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.postgres.concert.Concert;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

  Boolean existsByConcertName(String concertName);
}
