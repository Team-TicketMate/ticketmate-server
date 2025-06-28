package com.ticketmate.backend.domain.concert.service;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentOptionRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.concert.repository.ConcertAgentAvailabilityRepository;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.service.MemberService;
import com.ticketmate.backend.global.mapper.EntityMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConcertAgentAvailabilityService {
  private final ConcertAgentAvailabilityRepository concertAgentOptionRepository;
  private final ConcertService concertService;
  private final MemberService memberService;
  private final EntityMapper entityMapper;

  @Transactional
  public void setAcceptingOption(ConcertAgentOptionRequest request){
    Concert concert = concertService.findConcertById(request.getConcertId());
    Member agent = memberService.findMemberById(request.getAgentId());
    TicketOpenType ticketOpenType = request.getTicketOpenType();

    ConcertAgentAvailability concertAgentOption = concertAgentOptionRepository
        .findByConcertAndTicketOpenTypeAndAgent(concert, ticketOpenType, agent)
        .orElse(ConcertAgentAvailability.builder()
                .concert(concert)
                .ticketOpenType(ticketOpenType)
                .agent(agent)
                .build());

    concertAgentOption.setAccepting(request.isAccepting());
    concertAgentOptionRepository.save(concertAgentOption);
  }

  @Transactional(readOnly = true)
  public List<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId){
    return concertAgentOptionRepository
        .findAcceptingAgentByConcert(concertId)
        .stream().map(entityMapper::toAgentInfoResponse).toList();
  }

  @Transactional(readOnly = true)
  public Set<TicketOpenType> findAcceptingTicketOpenType(UUID concertId, UUID agentId){
    Concert concert = concertService.findConcertById(concertId);
    Member agent = memberService.findMemberById(agentId);

    return concertAgentOptionRepository.findAllByConcertAndAgent(concert, agent)
        .stream().map(ConcertAgentAvailability::getTicketOpenType)
        .collect(Collectors.toSet());
  }
}
