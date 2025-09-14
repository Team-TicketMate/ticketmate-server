package com.ticketmate.backend.api.application.controller.report;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.report.application.dto.request.ReportRequest;
import com.ticketmate.backend.report.application.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
@Tag(
    name = "사용자 신고 관련 API",
    description = "사용자 신고 관련 API 제공"
)
public class ReportController implements ReportControllerDocs {
  private final ReportService reportService;

  @Override
  @PostMapping
  @LogMonitoringInvocation
  public ResponseEntity<Void> createReport(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                           @RequestBody @Valid ReportRequest request) {
    reportService.createReport(customOAuth2User.getMember(), request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
