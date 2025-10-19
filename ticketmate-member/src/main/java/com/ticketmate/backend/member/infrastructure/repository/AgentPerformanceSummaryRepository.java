package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;

import java.util.UUID;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface AgentPerformanceSummaryRepository extends JpaRepository<AgentPerformanceSummary, UUID> {
  /**
   * 동시성 문제 방지를 위해 PESSIMISTIC_WRITE을 걸고 3초 이내 락을 획득하지 못하면 예외 발생
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
  AgentPerformanceSummary findByAgentId(UUID agentId);
}
