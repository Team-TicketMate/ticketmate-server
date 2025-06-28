package com.ticketmate.backend.domain.concert.controller;

import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.service.ConcertAgentAvailabilityService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert-agent-availability")
public class ConcertAgentAvailabilityController {
  private final ConcertAgentAvailabilityService concertAgentOptionService;

  @PostMapping
  public ResponseEntity<Void> setAcceptingOption(@RequestBody ConcertAgentAvailabilityRequest request){
    concertAgentOptionService.setAcceptingOption(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{concert-id}/agents")
  public ResponseEntity<List<ConcertAcceptingAgentInfo>> getAcceptingAgents(@PathVariable(value = "concert-id") UUID concertId){
    return ResponseEntity.ok().body(concertAgentOptionService.findAcceptingAgentByConcert(concertId));
  }
}
