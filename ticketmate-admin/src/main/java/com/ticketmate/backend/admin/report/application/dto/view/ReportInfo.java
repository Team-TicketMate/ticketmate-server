package com.ticketmate.backend.admin.report.application.dto.view;

import com.ticketmate.backend.report.core.constant.ReportReason;
import com.ticketmate.backend.report.core.constant.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public record ReportInfo(
    UUID reportId,
    UUID reporterId,
    UUID reportedMemberId,
    ReportReason reportReason,
    String description,
    ReportStatus reportStatus,
    Instant createdDate
) {
}
