package com.ticketmate.backend.domain.concert.repository;


import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertAgentAvailabilityRepository extends JpaRepository<ConcertAgentAvailability, UUID> {
  Optional<ConcertAgentAvailability> findByConcertConcertIdAndAgentMemberId(UUID concertId, UUID agentId);

}
