package com.ticketmate.backend.domain.concert.repository;

import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertDateRepository extends JpaRepository<ConcertDate, UUID> {

  List<ConcertDate> findAllByConcertConcertId(UUID concertId);

  // 공연PK + 공연일자로 조회
  Optional<ConcertDate> findByConcertConcertIdAndPerformanceDate(UUID concertId, LocalDateTime performanceDate);

  void deleteAllByConcertConcertId(UUID concertId);
}
