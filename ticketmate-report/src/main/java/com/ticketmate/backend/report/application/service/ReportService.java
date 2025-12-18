package com.ticketmate.backend.report.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.redis.application.annotation.RedisLock;
import com.ticketmate.backend.report.application.dto.request.ReportRequest;
import com.ticketmate.backend.report.infrastructure.entity.Report;
import com.ticketmate.backend.report.infrastructure.repository.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor

public class ReportService {
  private final ReportRepository reportRepository;
  private final MemberService memberService;

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('report', #reporter.memberId, #request.reportedMemberId)")
  public void createReport(Member reporter, ReportRequest request) {
    // 자기 자신 신고 검증
    if (reporter.getMemberId().equals(request.getReportedMemberId())) {
        log.error("자기 자신에 대한 신고 시도가 발생했습니다. 신고자 ID: {}", reporter.getMemberId());
      throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
    }

    Member reportedMember = memberService.findMemberById(request.getReportedMemberId());

    reportRepository.save(Report.create(reporter, reportedMember, request.getReportReason(), request.getDescription()));
  }
}
