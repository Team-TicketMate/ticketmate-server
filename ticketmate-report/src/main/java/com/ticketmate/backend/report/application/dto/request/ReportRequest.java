package com.ticketmate.backend.report.application.dto.request;

import com.ticketmate.backend.report.core.constant.ReportReason;
import lombok.*;

import java.util.UUID;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
  private UUID reportedUserId;

  private ReportReason reason;

  private String description;
}
