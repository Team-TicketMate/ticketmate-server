package com.ticketmate.backend.concerthall.infrastructure.repository;

import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertHallRepository extends JpaRepository<ConcertHall, UUID> {

  boolean existsByConcertHallName(String concertHallName);

  boolean existsByWebSiteUrl(String webSitUrl);
}
