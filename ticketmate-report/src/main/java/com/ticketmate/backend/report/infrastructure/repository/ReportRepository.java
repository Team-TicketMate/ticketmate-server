package com.ticketmate.backend.report.infrastructure.repository;

import com.ticketmate.backend.report.infrastructure.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
}
