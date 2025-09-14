package com.ticketmate.backend.admin.report.infrastructure.repository;

import com.ticketmate.backend.admin.report.application.dto.response.ReportInfoResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportFilteredResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepositoryCustom {
  Page<ReportFilteredResponse> filteredReports(Pageable pageable);

  Optional<ReportInfoResponse> findReportById(UUID reportId);
}
