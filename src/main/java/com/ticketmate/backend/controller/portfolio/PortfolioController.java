package com.ticketmate.backend.controller.portfolio;

import com.ticketmate.backend.controller.portfolio.docs.PortfolioControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.portfolio.request.PortfolioRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.service.portfolio.PortfolioService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
@Tag(
        name = "포트폴리오 API",
        description = "포트폴리오 관련 API 제공"
)
public class PortfolioController implements PortfolioControllerDocs {
    private final PortfolioService portfolioService;
    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<UUID> uploadPortfolio(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @ModelAttribute PortfolioRequest request) {
        Member member = customOAuth2User.getMember();
        return ResponseEntity.ok(portfolioService.uploadPortfolio(request, member));
    }
}
