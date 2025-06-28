package com.ticketmate.backend.domain.concert.repository;


import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertAgentAvailabilityRepository extends JpaRepository<ConcertAgentAvailability, UUID> {
  Optional<ConcertAgentAvailability> findByConcertAndTicketOpenTypeAndAgent(Concert concert, TicketOpenType ticketOpenType, Member agent);

  @Query("SELECT DISTINCT ca.agent "
         + "FROM ConcertAgentAvailability ca "
         + "WHERE ca.concert.concertId = :concertId "
         + "AND ca.isAccepting = true")
  List<Member> findAcceptingAgentByConcert(@Param("concertId") UUID concertId);

  List<ConcertAgentAvailability> findAllByConcertAndAgent(Concert concert, Member agent);
}
