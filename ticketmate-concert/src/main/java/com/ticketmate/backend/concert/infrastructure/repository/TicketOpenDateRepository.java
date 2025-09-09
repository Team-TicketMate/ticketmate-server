package com.ticketmate.backend.concert.infrastructure.repository;

import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketOpenDateRepository extends JpaRepository<TicketOpenDate, UUID> {

  // 티켓 오픈일 concertId로 조회
  List<TicketOpenDate> findAllByConcertConcertId(UUID concertId);

  Optional<TicketOpenDate> findByConcertConcertIdAndTicketOpenType(UUID concertId, TicketOpenType ticketOpenType);

  void deleteAllByConcertConcertId(UUID concertId);
}
