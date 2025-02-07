package com.ticketmate.backend.repository.postgres;

import com.ticketmate.backend.object.postgres.ConcertHall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConcertHallRepository extends JpaRepository<ConcertHall, UUID> {

}
