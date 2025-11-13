package com.ticketmate.backend.report.infrastructure.entity;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.report.core.constant.ReportReason;
import com.ticketmate.backend.report.core.constant.ReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Report extends BasePostgresEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reportId;

  // 신고 작성자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member reporter;

  // 신고 대상자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member reportedMember;

  // 신고 사유
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportReason reportReason;

  // 상세 내용
  @Column(length = 200)
  private String description;

  // 처리 상태
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportStatus reportStatus = ReportStatus.PENDING;

  public static Report create(Member reporter, Member reportedMember, ReportReason reportReason, String description) {
    return Report.builder()
        .reporter(reporter)
        .reportedMember(reportedMember)
        .reportReason(reportReason)
        .description(description)
        .build();
  }

  public void transitionReportStatus(ReportStatus reportStatus) {
    if (!canTransitionReportStatus(reportStatus)) {
        throw new CustomException(ErrorCode.REPORT_STATUS_TRANSITION_ERROR);
    }
    this.reportStatus = reportStatus;
  }

  private boolean canTransitionReportStatus(ReportStatus reportStatus) {
      if (reportStatus == null || this.reportStatus.equals(reportStatus)) {
          return false;
      }

      return switch (this.reportStatus) {
          case PENDING -> reportStatus == ReportStatus.IN_PROGRESS || reportStatus == ReportStatus.RESOLVED || reportStatus == ReportStatus.REJECTED;
          case IN_PROGRESS -> reportStatus == ReportStatus.RESOLVED || reportStatus == ReportStatus.REJECTED;
          case RESOLVED -> false;
          case REJECTED ->  false;
      };
  }
}
