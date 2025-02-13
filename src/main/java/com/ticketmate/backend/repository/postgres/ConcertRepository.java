package com.ticketmate.backend.repository.postgres;

import com.ticketmate.backend.object.postgres.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

    Boolean existsByConcertName(String concertName);
}
