package com.ticketmate.backend.admin.report.application.mapper;

import com.ticketmate.backend.admin.report.application.dto.response.ReportFilteredResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportInfoResponse;
import com.ticketmate.backend.admin.report.application.dto.view.ReportInfo;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportMapperImpl implements ReportMapper {
    @Override
    public ReportFilteredResponse toReportFilteredResponse(ReportInfo info) {
        return new ReportFilteredResponse(
            info.reportId(),
            info.reporterId(),
            info.reportedMemberId(),
            info.reportReason(),
            info.reportStatus(),
            TimeUtil.toLocalDateTime(info.createdDate())
        );
    }

    @Override
    public ReportInfoResponse toReportInfoResponse(ReportInfo info) {
        return new ReportInfoResponse(
            info.reportId(),
            info.reporterId(),
            info.reportedMemberId(),
            info.reportReason(),
            info.description(),
            info.reportStatus(),
            TimeUtil.toLocalDateTime(info.createdDate())
        );
    }
}
