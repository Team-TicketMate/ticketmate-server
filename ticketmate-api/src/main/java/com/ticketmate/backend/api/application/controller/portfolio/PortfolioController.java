package com.ticketmate.backend.api.application.controller.portfolio;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.portfolio.application.dto.request.PortfolioRequest;
import com.ticketmate.backend.portfolio.application.service.PortfolioService;
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
  @LogMonitoring
  public ResponseEntity<UUID> uploadPortfolio(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @ModelAttribute PortfolioRequest request) {
    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(portfolioService.uploadPortfolio(request, member));
  }
}
