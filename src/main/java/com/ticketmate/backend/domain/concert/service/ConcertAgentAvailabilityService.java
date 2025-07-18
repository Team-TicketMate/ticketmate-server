package com.ticketmate.backend.domain.concert.service;

import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAcceptingAgentFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.domain.concert.domain.entity.Concert;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertAgentAvailability;
import com.ticketmate.backend.domain.concert.repository.ConcertAgentAvailabilityRepository;
import com.ticketmate.backend.domain.concert.repository.ConcertAgentAvailabilityRepositoryCustom;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.service.MemberService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConcertAgentAvailabilityService {

  private final ConcertAgentAvailabilityRepository concertAgentAvailabilityRepository;
  private final ConcertAgentAvailabilityRepositoryCustom concertAgentAvailabilityRepositoryCustom;
  private final ConcertService concertService;
  private final MemberService memberService;

  /**
   * 공연별 대리인 수락 설정
   *
   * @param agent   대리인
   * @param request concertId 공연 PK
   *                accepting 수락여부
   *                introduction 한줄소개
   */
  @Transactional
  public void setAcceptingOption(Member agent, ConcertAgentAvailabilityRequest request) {
    Concert concert = concertService.findConcertById(request.getConcertId());
    memberService.validateMemberType(agent, MemberType.AGENT);

    ConcertAgentAvailability availability = concertAgentAvailabilityRepository
        .findByConcertAndAgent(concert, agent)
        .orElse(ConcertAgentAvailability.builder()
            .concert(concert)
            .agent(agent)
            .build()
        );

    availability.updateAcceptingStatus(request.getAccepting(), request.getAccepting() ? request.getIntroduction() : null);

    concertAgentAvailabilityRepository.save(availability);
  }

  /**
   * 공연별 대리인 수락 필터링 조회
   *
   * @param concertId 공연 PK
   * @param request   pageNumber
   *                  pageSize
   *                  sortField
   *                  sortDirection
   */
  @Transactional(readOnly = true)
  public Slice<ConcertAcceptingAgentResponse> findAcceptingAgentByConcert(UUID concertId, ConcertAcceptingAgentFilteredRequest request) {
    Pageable pageable = request.toPageable();
    return concertAgentAvailabilityRepositoryCustom.findAcceptingAgentByConcert(concertId, pageable);
  }
}
