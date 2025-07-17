package com.ticketmate.backend.domain.concert.controller;

import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAcceptingAgentFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.domain.concert.service.ConcertAgentAvailabilityService;
import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert-agent-availability")
@Tag(
    name = "공연별 대리인 수락 관련 API",
    description = "공연별 대리인 수락 관련 API 제공"
)
public class ConcertAgentAvailabilityController implements ConcertAgentAvailabilityControllerDocs {

  private final ConcertAgentAvailabilityService concertAgentAvailabilityService;

  @Override
  @PostMapping
  @LogMonitoringInvocation
  public ResponseEntity<Void> setAcceptingOption(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody ConcertAgentAvailabilityRequest request) {
    concertAgentAvailabilityService.setAcceptingOption(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/{concert-id}/agents")
  @LogMonitoringInvocation
  public ResponseEntity<Slice<ConcertAcceptingAgentResponse>> filteredAcceptingAgents(
      @PathVariable(value = "concert-id") UUID concertId,
      @ParameterObject @Valid ConcertAcceptingAgentFilteredRequest request) {
    return ResponseEntity.ok().body(concertAgentAvailabilityService.findAcceptingAgentByConcert(concertId, request));
  }
}
