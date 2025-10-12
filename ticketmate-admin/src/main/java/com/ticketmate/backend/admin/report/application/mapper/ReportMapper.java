package com.ticketmate.backend.admin.report.application.mapper;

import com.ticketmate.backend.admin.report.application.dto.response.ReportFilteredResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportInfoResponse;
import com.ticketmate.backend.admin.report.application.dto.view.ReportInfo;

public interface ReportMapper {
    /**
     * ReportInfo -> ReportFilteredResponse
     * Instant -> LocalDateTime 변환
     */
    ReportFilteredResponse toReportFilteredResponse(ReportInfo info);

    /**
     * ReportInfo -> ReportInfoResponse
     * Instant -> LocalDateTime 변환
     */
    ReportInfoResponse toReportInfoResponse(ReportInfo info);
}
