package com.ticketmate.backend.api.application.controller.concert;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.concert.application.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.service.ConcertService;
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
@RequestMapping("/api/concert")
@Tag(
    name = "공연 관련 API",
    description = "공연 관련 API 제공"
)
public class ConcertController implements ConcertControllerDocs {

  private final ConcertService concertService;

  @Override
  @GetMapping(value = "")
  @LogMonitoring
  public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ConcertFilteredRequest request) {
    return ResponseEntity.ok(concertService.filteredConcert(request));
  }

  @Override
  @GetMapping(value = "{concert-id}")
  @LogMonitoring
  public ResponseEntity<ConcertInfoResponse> getConcertInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "concert-id") UUID concertId) {
    return ResponseEntity.ok(concertService.getConcertInfo(concertId));
  }
}
