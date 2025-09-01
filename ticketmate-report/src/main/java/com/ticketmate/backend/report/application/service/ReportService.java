package com.ticketmate.backend.report.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import com.ticketmate.backend.report.application.dto.request.ReportRequest;
import com.ticketmate.backend.report.infrastructure.entity.Report;
import com.ticketmate.backend.report.infrastructure.repository.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
  private final ReportRepository reportRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public void createReport(UUID reporterId, ReportRequest request) {
    Member reporter = memberRepository.findById(reporterId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Member reportedUser = memberRepository.findById(request.getReportedUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    reportRepository.save(Report.of(reporter, reportedUser, request));
  }
}
