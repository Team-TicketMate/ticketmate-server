package com.ticketmate.backend.report.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.report.application.dto.request.ReportRequest;
import com.ticketmate.backend.report.core.constant.ReportReason;
import com.ticketmate.backend.report.core.constant.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class Report extends BasePostgresEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID reportId;

  // 신고 작성자
  @ManyToOne(fetch = FetchType.LAZY)
  private Member reporter;

  // 신고 대상자
  @ManyToOne(fetch = FetchType.LAZY)
  private Member reportedUser;

  // 신고 사유
  @Enumerated(EnumType.STRING)
  private ReportReason reason;

  // 상세 내용
  private String description;

  // 처리 상태
  @Builder.Default
  private ReportStatus status = ReportStatus.PENDING;

  public static Report of(Member reporter, Member reportedUser, ReportRequest request) {
    return Report.builder()
        .reporter(reporter)
        .reportedUser(reportedUser)
        .reason(request.getReason())
        .description(request.getDescription())
        .build();
  }

  public void updateStatus(ReportStatus status) {
    this.status = status;
  }
}
