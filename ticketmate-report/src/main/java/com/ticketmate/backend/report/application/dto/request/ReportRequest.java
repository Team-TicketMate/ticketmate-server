package com.ticketmate.backend.report.application.dto.request;

import com.ticketmate.backend.report.core.constant.ReportReason;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
  @NotNull
  private UUID reportedMemberId;

  @NotNull
  private ReportReason reportReason;

  private String description;
}
