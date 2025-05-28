package com.ticketmate.backend.controller.admin;

import com.ticketmate.backend.controller.admin.docs.AdminControllerDocs;
import com.ticketmate.backend.object.dto.admin.request.*;
import com.ticketmate.backend.object.dto.admin.response.ConcertHallFilteredAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.service.admin.AdminService;
import com.ticketmate.backend.service.concert.ConcertService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(
        name = "관리자 API",
        description = "관리자 페이지 관련 API 제공"
)
public class AdminController implements AdminControllerDocs {

    private final AdminService adminService;
    private final ConcertService concertService;

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
    public ResponseEntity<Page<ConcertHallFilteredAdminResponse>> filteredConcertHall(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid ConcertHallFilteredRequest request) {
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
        adminService.saveConcertInfo(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping(value = "/concert")
    @LogMonitoringInvocation
    public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid ConcertFilteredRequest request) {
        return ResponseEntity.ok(concertService.filteredConcert(request));
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
            @Valid @ModelAttribute PortfolioFilteredRequest request) {
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
    public ResponseEntity<UUID> reviewPortfolio(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable(value = "portfolio-id") UUID portfolioId,
            @RequestBody @Valid PortfolioStatusUpdateRequest request) {
        return ResponseEntity.ok(adminService.reviewPortfolioCompleted(portfolioId, request));
    }
}
