package com.ticketmate.backend.report.infrastructure.repository;

import com.ticketmate.backend.report.infrastructure.entity.Report;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {
}
