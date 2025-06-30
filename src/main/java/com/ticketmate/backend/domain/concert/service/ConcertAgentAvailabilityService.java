package com.ticketmate.backend.domain.concert.service;

import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAcceptingAgentRequest;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.concert.repository.ConcertAgentAvailabilityRepository;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.util.common.PageableUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    availability.updateAcceptingStatus(request.getAccepting(), request.getAccepting() ? request.getIntroduction() : null);

    concertAgentOptionRepository.save(availability);
  }

  @Transactional(readOnly = true)
  public Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, ConcertAcceptingAgentRequest request){
    Pageable pageable = PageableUtil.createPageable(
        request.getPageNumber(),
        request.getPageSize(),
        "createdDate",
        "ASC",
        "createdDate"
    );  // TODO: 정렬 기준 정해지면 추후 변경

    return concertAgentOptionRepository.findAcceptingAgentByConcert(concertId, pageable);

  }
}
