package com.ticketmate.backend.admin.report.infrastructure.repository;

import com.ticketmate.backend.admin.report.application.dto.response.ReportDetailResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepositoryCustom {
  Page<ReportListResponse> filteredReports(Pageable pageable);

  Optional<ReportDetailResponse> findReportById(UUID reportId);
}
