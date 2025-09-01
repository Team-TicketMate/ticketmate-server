package com.ticketmate.backend.admin.report.application.service;

import com.ticketmate.backend.admin.report.application.dto.request.ReportFilteredRequest;
import com.ticketmate.backend.admin.report.application.dto.request.ReportUpdateRequest;
import com.ticketmate.backend.admin.report.application.dto.response.ReportDetailResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportListResponse;
import com.ticketmate.backend.admin.report.infrastructure.repository.ReportRepositoryCustom;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.report.infrastructure.entity.Report;
import com.ticketmate.backend.report.infrastructure.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportAdminService {
  private final ReportRepository reportRepository;
  private final ReportRepositoryCustom reportRepositoryCustom;

  @Transactional(readOnly = true)
  public Page<ReportListResponse> getReports(ReportFilteredRequest request) {
    return reportRepositoryCustom.filteredReports(request.toPageable());
  }

  @Transactional(readOnly = true)
  public ReportDetailResponse getReport(UUID reportId) {
    return reportRepositoryCustom.findReportById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
  }

  @Transactional
  public void updateReport(UUID reportId, ReportUpdateRequest request) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    report.updateStatus(request.getReportStatus());
  }

  @Transactional
  public void deleteReport(UUID reportId) {
    reportRepository.deleteById(reportId);
    // TODO: soft delete 고려
  }
}
