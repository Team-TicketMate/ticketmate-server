package com.ticketmate.backend.api.application.controller.admin;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.admin.concert.application.dto.request.ConcertInfoEditRequest;
import com.ticketmate.backend.admin.concert.application.dto.request.ConcertInfoRequest;
import com.ticketmate.backend.admin.concert.application.service.ConcertAdminService;
import com.ticketmate.backend.admin.concerthall.application.dto.request.ConcertHallInfoEditRequest;
import com.ticketmate.backend.admin.concerthall.application.dto.request.ConcertHallInfoRequest;
import com.ticketmate.backend.admin.concerthall.application.service.ConcertHallAdminService;
import com.ticketmate.backend.admin.portfolio.application.dto.request.PortfolioFilteredRequest;
import com.ticketmate.backend.admin.portfolio.application.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.service.PortfolioAdminService;
import com.ticketmate.backend.admin.report.application.dto.request.ReportFilteredRequest;
import com.ticketmate.backend.admin.report.application.dto.request.ReportUpdateRequest;
import com.ticketmate.backend.admin.report.application.dto.response.ReportFilteredResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportInfoResponse;
import com.ticketmate.backend.admin.report.application.service.ReportAdminService;
import com.ticketmate.backend.admin.sms.application.dto.response.CoolSmsBalanceResponse;
import com.ticketmate.backend.admin.sms.application.service.SmsAdminService;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.concert.application.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concerthall.application.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallFilteredResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(
    name = "관리자 API",
    description = "관리자 페이지 관련 API 제공"
)
public class AdminController implements AdminControllerDocs {

  private final ConcertAdminService concertAdminService;
  private final ConcertHallAdminService concertHallAdminService;
  private final PortfolioAdminService portfolioAdminService;
  private final SmsAdminService smsAdminService;
  private final ReportAdminService reportAdminService;

  /*
  ======================================공연장======================================
   */

  @Override
  @PostMapping(value = "/concert-hall")
  @LogMonitoring
  public ResponseEntity<Void> saveHallInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody ConcertHallInfoRequest request) {
    concertHallAdminService.saveConcertHallInfo(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  @GetMapping(value = "/concert-hall")
  @LogMonitoring
  public ResponseEntity<Page<ConcertHallFilteredResponse>> filteredConcertHall(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertHallFilteredRequest request) {
    return ResponseEntity.ok(concertHallAdminService.filteredConcertHall(request));
  }

  @Override
  @PatchMapping(value = "/concert-hall/{concert-hall-id}")
  @LogMonitoring
  public ResponseEntity<Void> editConcertHallInfo(
      @PathVariable("concert-hall-id") UUID concertHallId,
      @Valid @RequestBody ConcertHallInfoEditRequest request) {
    concertHallAdminService.editConcertHallInfo(concertHallId, request);
    return ResponseEntity.ok().build();
  }

  /*
  ======================================공연======================================
   */

  @Override
  @PostMapping(value = "/concert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<Void> saveConcertInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @ModelAttribute ConcertInfoRequest request) {
    concertAdminService.saveConcert(request);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping(value = "/concert")
  @LogMonitoring
  public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertFilteredRequest request) {
    return ResponseEntity.ok(concertAdminService.filteredConcert(request));
  }

  @Override
  @GetMapping(value = "/concert/{concert-id}")
  @LogMonitoring
  public ResponseEntity<ConcertInfoResponse> getConcertInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "concert-id") UUID concertId
  ) {
    return ResponseEntity.ok(concertAdminService.getConcertInfo(concertId));
  }

  @Override
  @PatchMapping(value = "/concert/{concert-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<Void> editConcertInfo(
      @PathVariable("concert-id") UUID concertId,
      @Valid @ModelAttribute ConcertInfoEditRequest request) {
    concertAdminService.editConcertInfo(concertId, request);
    return ResponseEntity.ok().build();
  }

  /*
  ======================================포트폴리오======================================
   */

  @Override
  @GetMapping(value = "/portfolio")
  @LogMonitoring
  public ResponseEntity<Page<PortfolioFilteredAdminResponse>> filteredPortfolio(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid PortfolioFilteredRequest request) {
    return ResponseEntity.ok(portfolioAdminService.filteredPortfolio(request));
  }

  @Override
  @GetMapping(value = "/portfolio/{portfolio-id}")
  @LogMonitoring
  public ResponseEntity<PortfolioAdminResponse> getPortfolioInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "portfolio-id") UUID portfolioId) {
    return ResponseEntity.ok(portfolioAdminService.getPortfolio(portfolioId));
  }

  @Override
  @PatchMapping(value = "/portfolio/{portfolio-id}/status")
  @LogMonitoring
  public ResponseEntity<Void> changePortfolioStatus(
      @PathVariable(value = "portfolio-id") UUID portfolioId,
      @RequestBody @Valid PortfolioStatusUpdateRequest request) {
    portfolioAdminService.changePortfolioStatus(portfolioId, request);
    return ResponseEntity.ok().build();
  }

  /*
  ======================================SMS======================================
   */

  @Override
  @GetMapping("/cool-sms/balance")
  @LogMonitoring
  public ResponseEntity<CoolSmsBalanceResponse> getBalance(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok(smsAdminService.getBalance());
  }

  /*
  ======================================신고======================================
   */

  @Override
  @GetMapping("/report")
  @LogMonitoring
  public ResponseEntity<Page<ReportFilteredResponse>> getReports(@ParameterObject @Valid ReportFilteredRequest request) {
    return ResponseEntity.ok(reportAdminService.getReports(request));
  }

  @Override
  @GetMapping("/report/{report-id}")
  @LogMonitoring
  public ResponseEntity<ReportInfoResponse> getReport(@PathVariable(value = "report-id") UUID reportId) {
    return ResponseEntity.ok(reportAdminService.getReport(reportId));
  }

  @Override
  @PutMapping("/report/{report-id}")
  @LogMonitoring
  public ResponseEntity<Void> updateReport(@PathVariable(value = "report-id") UUID reportId, @RequestBody @Valid ReportUpdateRequest request) {
    reportAdminService.updateReport(reportId, request);
    return ResponseEntity.noContent().build();
  }
}
