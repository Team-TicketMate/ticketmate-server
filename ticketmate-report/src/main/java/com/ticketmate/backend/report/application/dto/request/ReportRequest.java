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
  private UUID reportedUserId;

  @NotNull
  private ReportReason reason;

  private String description;
}
