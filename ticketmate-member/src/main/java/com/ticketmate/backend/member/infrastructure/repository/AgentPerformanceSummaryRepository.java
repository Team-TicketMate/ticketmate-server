package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;

import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface AgentPerformanceSummaryRepository extends JpaRepository<AgentPerformanceSummary, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  AgentPerformanceSummary findByAgentId(UUID agentId);
}
