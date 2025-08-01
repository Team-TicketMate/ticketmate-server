package com.ticketmate.backend.api.application.controller.concerthall;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.concerthall.application.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.concerthall.application.dto.response.ConcertHallInfoResponse;
import com.ticketmate.backend.concerthall.application.service.ConcertHallService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert-hall")
@Tag(
    name = "공연장 관련 API",
    description = "공연장 관련 API 제공"
)
public class ConcertHallController implements ConcertHallControllerDocs {

  private final ConcertHallService concertHallService;

  @Override
  @GetMapping("")
  @LogMonitoringInvocation
  public ResponseEntity<Page<ConcertHallFilteredResponse>> filteredConcertHall(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertHallFilteredRequest request) {
    return ResponseEntity.ok(concertHallService.filteredConcertHall(request));
  }

  @Override
  @GetMapping(value = "/{concert-hall-id}")
  @LogMonitoringInvocation
  public ResponseEntity<ConcertHallInfoResponse> getConcertHallInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable("concert-hall-id") UUID concertHallId) {
    return ResponseEntity.ok(concertHallService.getConcertHallInfo(concertHallId));
  }

}
