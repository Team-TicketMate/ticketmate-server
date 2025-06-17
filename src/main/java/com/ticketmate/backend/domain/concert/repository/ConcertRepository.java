package com.ticketmate.backend.domain.concert.repository;

import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

  Boolean existsByConcertName(String concertName);
}
