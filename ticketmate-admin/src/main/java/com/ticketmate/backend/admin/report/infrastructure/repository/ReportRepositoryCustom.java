package com.ticketmate.backend.admin.report.infrastructure.repository;

import com.ticketmate.backend.admin.report.application.dto.view.ReportInfo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryCustom {
  Page<ReportInfo> filteredReports(Pageable pageable);

  Optional<ReportInfo> findReportById(UUID reportId);
}
