package com.ticketmate.backend.admin.report.application.dto.request;

import com.ticketmate.backend.report.core.constant.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
