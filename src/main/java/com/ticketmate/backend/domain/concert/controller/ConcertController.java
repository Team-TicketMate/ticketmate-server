package com.ticketmate.backend.domain.concert.controller;

import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.concert.service.ConcertService;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert")
@Tag(
    name = "공연 관련 API",
    description = "공연 관련 API 제공"
)
public class ConcertController implements ConcertControllerDocs {

  private final ConcertService concertService;

  @Override
  @GetMapping(value = "")
  @LogMonitoringInvocation
  public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertFilteredRequest request) {
    return ResponseEntity.ok(concertService.filteredConcert(request));
  }

  @Override
  @GetMapping(value = "{concert-id}")
  @LogMonitoringInvocation
  public ResponseEntity<ConcertInfoResponse> getConcertInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "concert-id") UUID concertId) {
    return ResponseEntity.ok(concertService.getConcertInfo(concertId));
  }
}
