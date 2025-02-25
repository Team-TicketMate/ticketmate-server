package com.ticketmate.backend.controller.admin;

import com.ticketmate.backend.controller.admin.docs.AdminControllerDocs;
import com.ticketmate.backend.object.dto.admin.request.PortfolioSearchRequest;
import com.ticketmate.backend.object.dto.admin.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
import com.ticketmate.backend.service.admin.AdminService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @Override
    @PostMapping(value = "/concert-hall/save")
    @LogMonitoringInvocation
    public ResponseEntity<Void> saveHallInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @RequestBody ConcertHallInfoRequest request) {
        adminService.saveHallInfo(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping(value = "/concert/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<Void> saveConcertInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @ModelAttribute ConcertInfoRequest request) {
        adminService.saveConcertInfo(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping(value = "/portfolio/list")
    @LogMonitoringInvocation
    public ResponseEntity<Page<PortfolioListForAdminResponse>> getPortfolioList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @ModelAttribute PortfolioSearchRequest request) {
        return ResponseEntity.ok(adminService.getPortfolioList(request));
    }

    @Override
    @GetMapping(value = "/portfolio/list/{portfolio-id}")
    @LogMonitoringInvocation
    public ResponseEntity<PortfolioForAdminResponse> getPortfolioInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable(value = "portfolio-id") UUID portfolioId) {
        return ResponseEntity.ok(adminService.getPortfolio(portfolioId));
    }

    @Override
    @PatchMapping(value = "/portfolio/list/{portfolio-id}")
    @LogMonitoringInvocation
    public ResponseEntity<UUID> reviewPortfolio(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable(value = "portfolio-id") UUID portfolioId,
            @RequestBody @Valid PortfolioStatusUpdateRequest request) {
        return ResponseEntity.ok(adminService.reviewPortfolioCompleted(portfolioId, request));
    }
}
