package com.ticketmate.backend.domain.concert.service;

import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.concert.repository.ConcertAgentAvailabilityRepository;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConcertAgentAvailabilityService {
  private final ConcertAgentAvailabilityRepository concertAgentOptionRepository;
  private final ConcertService concertService;

  @Transactional
  public void setAcceptingOption(Member agent, ConcertAgentAvailabilityRequest request){
    Concert concert = concertService.findConcertById(request.getConcertId());

    ConcertAgentAvailability availability  = concertAgentOptionRepository
        .findByConcertAndAgent(concert, agent)
        .orElse(ConcertAgentAvailability.builder()
                .concert(concert)
                .agent(agent)
                .build());

    availability.setAccepting(request.isAccepting());
    availability.setIntroduction(request.isAccepting() ? request.getIntroduction() : null);

    concertAgentOptionRepository.save(availability);
  }

  @Transactional(readOnly = true)
  public List<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId){
    return concertAgentOptionRepository.findAcceptingAgentByConcert(concertId);
  }
}
