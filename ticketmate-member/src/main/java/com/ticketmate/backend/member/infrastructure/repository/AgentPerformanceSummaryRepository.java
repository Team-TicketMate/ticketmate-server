package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentPerformanceSummaryRepository extends JpaRepository<AgentPerformanceSummary, UUID> {

}
