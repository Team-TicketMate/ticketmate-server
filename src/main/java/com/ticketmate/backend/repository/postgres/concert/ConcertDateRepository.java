package com.ticketmate.backend.repository.postgres.concert;

import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcertDateRepository extends JpaRepository<ConcertDate, UUID> {

    List<ConcertDate> findAllByConcert_ConcertId(UUID concertId);

    // 공연PK + 공연일자로 조회
    Optional<ConcertDate> findByConcert_ConcertIdAndPerformanceDate(UUID concertId, LocalDateTime performanceDate);
}
