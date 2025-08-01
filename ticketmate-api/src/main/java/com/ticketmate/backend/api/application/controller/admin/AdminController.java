package com.ticketmate.backend.api.application.controller.admin;

import com.ticketmate.backend.admin.application.dto.request.ConcertHallInfoEditRequest;
import com.ticketmate.backend.admin.application.dto.request.ConcertHallInfoRequest;
import com.ticketmate.backend.admin.application.dto.request.ConcertInfoEditRequest;
import com.ticketmate.backend.admin.application.dto.request.ConcertInfoRequest;
import com.ticketmate.backend.admin.application.dto.request.PortfolioFilteredRequest;
import com.ticketmate.backend.admin.application.dto.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.admin.application.dto.response.CoolSmsBalanceResponse;
import com.ticketmate.backend.admin.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.application.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.admin.application.service.AdminService;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
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

  private final AdminService adminService;

  /*
  ======================================공연장======================================
   */

  @Override
  @PostMapping(value = "/concert-hall")
  @LogMonitoringInvocation
  public ResponseEntity<Void> saveHallInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody ConcertHallInfoRequest request) {
    adminService.saveConcertHallInfo(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  @GetMapping(value = "/concert-hall")
  @LogMonitoringInvocation
  public ResponseEntity<Page<ConcertHallFilteredResponse>> filteredConcertHall(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertHallFilteredRequest request) {
    return ResponseEntity.ok(adminService.filteredConcertHall(request));
  }

  @Override
  @PatchMapping(value = "/concert-hall/{concert-hall-id}")
  @LogMonitoringInvocation
  public ResponseEntity<Void> editConcertHallInfo(
      @PathVariable("concert-hall-id") UUID concertHallId,
      @Valid @RequestBody ConcertHallInfoEditRequest request) {
    adminService.editConcertHallInfo(concertHallId, request);
    return ResponseEntity.ok().build();
  }

  /*
  ======================================공연======================================
   */

  @Override
  @PostMapping(value = "/concert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<Void> saveConcertInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @ModelAttribute ConcertInfoRequest request) {
    adminService.saveConcert(request);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping(value = "/concert")
  @LogMonitoringInvocation
  public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertFilteredRequest request) {
    return ResponseEntity.ok(adminService.filteredConcert(request));
  }

  @Override
  @GetMapping(value = "/concert/{concert-id}")
  @LogMonitoringInvocation
  public ResponseEntity<ConcertInfoResponse> getConcertInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "concert-id") UUID concertId
  ) {
    return ResponseEntity.ok(adminService.getConcertInfo(concertId));
  }

  @Override
  @PatchMapping(value = "/concert/{concert-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<Void> editConcertInfo(
      @PathVariable("concert-id") UUID concertId,
      @Valid @ModelAttribute ConcertInfoEditRequest request) {
    adminService.editConcertInfo(concertId, request);
    return ResponseEntity.ok().build();
  }

  /*
  ======================================포트폴리오======================================
   */

  @Override
  @GetMapping(value = "/portfolio")
  @LogMonitoringInvocation
  public ResponseEntity<Page<PortfolioFilteredAdminResponse>> filteredPortfolio(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid PortfolioFilteredRequest request) {
    return ResponseEntity.ok(adminService.filteredPortfolio(request));
  }

  @Override
  @GetMapping(value = "/portfolio/{portfolio-id}")
  @LogMonitoringInvocation
  public ResponseEntity<PortfolioForAdminResponse> getPortfolioInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "portfolio-id") UUID portfolioId) {
    return ResponseEntity.ok(adminService.getPortfolio(portfolioId));
  }

  @Override
  @PatchMapping(value = "/portfolio/{portfolio-id}")
  @LogMonitoringInvocation
  public ResponseEntity<Void> reviewPortfolio(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "portfolio-id") UUID portfolioId,
      @RequestBody @Valid PortfolioStatusUpdateRequest request) {
    adminService.reviewPortfolioCompleted(portfolioId, request);
    return ResponseEntity.ok().build();
  }

  /*
  ======================================SMS======================================
   */

  @Override
  @GetMapping("/cool-sms/balance")
  @LogMonitoringInvocation
  public ResponseEntity<CoolSmsBalanceResponse> getBalance(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok(adminService.getBalance());
  }
}
