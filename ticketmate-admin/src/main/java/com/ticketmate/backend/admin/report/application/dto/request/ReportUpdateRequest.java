package com.ticketmate.backend.admin.report.application.dto.request;

import com.ticketmate.backend.report.core.constant.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReportUpdateRequest {
  @NotNull
  private ReportStatus reportStatus;
  // TODO: 추후 수정 기능 확장 고려
}
