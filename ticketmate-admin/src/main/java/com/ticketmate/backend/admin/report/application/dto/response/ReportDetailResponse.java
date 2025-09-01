package com.ticketmate.backend.admin.report.application.dto.response;

import com.ticketmate.backend.report.core.constant.ReportReason;
import com.ticketmate.backend.report.core.constant.ReportStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailResponse {
  // 신고 ID
  private UUID reportId;

  // 신고자 ID
  private UUID reporterId;

  // 피신고자 ID
  private UUID reportedUserId;

  // 신고 사유
  private ReportReason reason;

  // 신고 상세 내용
  private String description;

  // 신고 처리 상태
  private ReportStatus status;

  // 신고 생성 일시
  private LocalDateTime createdDate;

  // TODO: 추후 UI 확정되면 변경 (사용자도 ID 대신 dto로 변경)
}
