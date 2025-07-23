package com.ticketmate.backend.domain.concert.repository;

import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertAgentAvailabilityRepository extends JpaRepository<ConcertAgentAvailability, UUID> {

  Optional<ConcertAgentAvailability> findByConcertAndAgent(Concert concert, Member agent);

}
