package com.ticketmate.backend.admin.report.application.dto.request;

import com.ticketmate.backend.report.core.constant.ReportStatus;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReportUpdateRequest {
  private ReportStatus reportStatus;
  // TODO: 추후 수정 기능 확장 고려
}
