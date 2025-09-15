package com.ticketmate.backend.report.application.dto.request;

import com.ticketmate.backend.report.core.constant.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

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
