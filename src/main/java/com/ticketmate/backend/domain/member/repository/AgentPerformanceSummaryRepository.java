package com.ticketmate.backend.domain.member.repository;

import com.ticketmate.backend.domain.member.domain.entity.AgentPerformanceSummary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentPerformanceSummaryRepository extends JpaRepository<AgentPerformanceSummary, UUID> {

}
