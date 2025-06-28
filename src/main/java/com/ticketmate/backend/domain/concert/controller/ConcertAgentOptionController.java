package com.ticketmate.backend.domain.concert.controller;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentOptionRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.service.ConcertAgentOptionService;
import java.util.List;
import java.util.Set;
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
@RequestMapping("/api/concert-agent-option")
public class ConcertAgentOptionController {
  private final ConcertAgentOptionService concertAgentOptionService;

  @PostMapping("/requestability")
  public ResponseEntity<Void> setAcceptingOption(@RequestBody ConcertAgentOptionRequest request){
    concertAgentOptionService.setAcceptingOption(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{concert-id}/agents")
  public ResponseEntity<List<ConcertAcceptingAgentInfo>> getAcceptingAgents(@PathVariable(value = "concert-id") UUID concertId){
    return ResponseEntity.ok().body(concertAgentOptionService.findAcceptingAgentByConcert(concertId));
  }

  @GetMapping("/{concert-id}/agents/{agent-id}/types")
  public ResponseEntity<Set<TicketOpenType>> getAcceptingTicketOpenTypes(@PathVariable(value = "concert-id") UUID concertId, @PathVariable(value = "agent-id") UUID agentId){
    return ResponseEntity.ok().body(concertAgentOptionService.findAcceptingTicketOpenType(concertId, agentId));
  }
}
