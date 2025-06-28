package com.ticketmate.backend.domain.concert.repository;


import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentOption;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertAgentOptionRepository extends JpaRepository<ConcertAgentOption, UUID> {
  Optional<ConcertAgentOption> findByConcertAndTicketOpenTypeAndAgent(Concert concert, TicketOpenType ticketOpenType, Member agent);

  @Query("SELECT DISTINCT cao.agent "
         + "FROM ConcertAgentOption cao "
         + "WHERE cao.concert.concertId = :concertId "
         + "AND cao.isAccepting = true")
  List<Member> findAcceptingAgentByConcert(@Param("concertId") UUID concertId);

  List<ConcertAgentOption> findAllByConcertAndAgent(Concert concert, Member agent);
}
