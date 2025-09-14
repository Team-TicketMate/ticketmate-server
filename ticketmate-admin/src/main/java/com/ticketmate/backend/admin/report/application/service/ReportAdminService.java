package com.ticketmate.backend.admin.report.application.service;

import com.ticketmate.backend.admin.report.application.dto.request.ReportFilteredRequest;
import com.ticketmate.backend.admin.report.application.dto.request.ReportUpdateRequest;
import com.ticketmate.backend.admin.report.application.dto.response.ReportInfoResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportFilteredResponse;
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

    /**
     * 신고 내역 전체 조회
     *
     * @param request reportId
     *                reporterId
     *                reportedMemberId
     *                reportReason
     *                reportStatus
     *                createdDate
     */
  @Transactional(readOnly = true)
  public Page<ReportFilteredResponse> getReports(ReportFilteredRequest request) {
    return reportRepositoryCustom.filteredReports(request.toPageable());
  }

    /**
     * 신고 내역 상세 조회
     *
     * @param reportId
     */
  @Transactional(readOnly = true)
  public ReportInfoResponse getReport(UUID reportId) {
    return reportRepositoryCustom.findReportById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
  }

    /**
     * 신고 수정 (상태 변경)
     *
     * @param reportId
     * @param request reportStatus
     */
  @Transactional
  public void updateReport(UUID reportId, ReportUpdateRequest request) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    report.transitionReportStatus(request.getReportStatus());
  }
}
