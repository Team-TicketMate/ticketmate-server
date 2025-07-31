package com.ticketmate.backend.concert.infrastructure.repository;

import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

  Boolean existsByConcertName(String concertName);
}
