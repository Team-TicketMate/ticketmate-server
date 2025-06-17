package com.ticketmate.backend.domain.portfolio.controller;

import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.portfolio.domain.dto.request.PortfolioRequest;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.portfolio.service.PortfolioService;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
