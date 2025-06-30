package com.ticketmate.backend.domain.concert.repository;


import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertAgentAvailabilityRepository extends JpaRepository<ConcertAgentAvailability, UUID> {
  Optional<ConcertAgentAvailability> findByConcertAndAgent(Concert concert, Member agent);

  @Query("SELECT new com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo("
         + "ca.agent.memberId, ca.agent.nickname, ca.agent.profileUrl, "
         + "COALESCE(ca.introduction, '')) "
         + "FROM ConcertAgentAvailability ca "
         + "WHERE ca.concert.concertId = :concertId "
         + "AND ca.accepting = true")
  Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(@Param("concertId") UUID concertId, Pageable pageable);
}
