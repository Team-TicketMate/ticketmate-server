package com.ticketmate.backend.domain.concerthall.controller;

import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.concerthall.domain.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallInfoResponse;
import com.ticketmate.backend.domain.concerthall.service.ConcertHallService;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
      @Valid @ModelAttribute ConcertHallFilteredRequest request) {
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
