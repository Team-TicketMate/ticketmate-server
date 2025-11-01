package com.ticketmate.backend.concertagentavailability.infrastructure.repository;

import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concertagentavailability.infrastructure.entity.ConcertAgentAvailability;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertAgentAvailabilityRepository extends JpaRepository<ConcertAgentAvailability, UUID> {

  Optional<ConcertAgentAvailability> findByConcertAndAgent(Concert concert, Member agent);

}
