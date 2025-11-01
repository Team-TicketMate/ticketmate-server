package com.ticketmate.backend.concertagentavailability.application.service;

import com.ticketmate.backend.concert.application.service.ConcertService;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concertagentavailability.application.dto.request.ConcertAcceptingAgentFilteredRequest;
import com.ticketmate.backend.concertagentavailability.application.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.concertagentavailability.application.dto.request.ConcertStatusFilteredRequest;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AcceptingConcertInfoResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAgentStatusResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAgentStatusInfo;
import com.ticketmate.backend.concertagentavailability.application.mapper.ConcertAgentAvailabilityMapper;
import com.ticketmate.backend.concertagentavailability.infrastructure.entity.ConcertAgentAvailability;
import com.ticketmate.backend.concertagentavailability.infrastructure.repository.ConcertAgentAvailabilityRepository;
import com.ticketmate.backend.concertagentavailability.infrastructure.repository.ConcertAgentAvailabilityRepositoryCustom;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
  private final ConcertAgentAvailabilityMapper availabilityMapper;

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
    Slice<ConcertAcceptingAgentInfo> infoSlice = concertAgentAvailabilityRepositoryCustom
        .findAcceptingAgentByConcert(concertId, request.toPageable());
    return infoSlice.map(availabilityMapper::toConcertAcceptingAgentResponse);
  }

  /**
   * 대리인 on/off 설정을 위한 공연 목록 조회
   *
   * @param agentId 현재 로그인한 대리인의 memberId
   * @param request pageNumber
   *                pageSize
   * @return Slice<MyConcertResponse>
   */
  @Transactional(readOnly = true)
  public Slice<ConcertAgentStatusResponse> getMyConcertList(UUID agentId, ConcertStatusFilteredRequest request) {
    Slice<ConcertAgentStatusInfo> infoSlice = concertAgentAvailabilityRepositoryCustom.findMyConcertList(agentId, request.toPageable());
    return infoSlice.map(availabilityMapper::toConcertAgentStatusResponse);
  }

  /**
   * 대리인 on 설정된 모집 중 공연 목록 조회
   *
   * @param agentId 현재 로그인한 대리인의 memberId
   * @param request pageNumber
   *                pageSize
   * @return Slice<MyConcertResponse>
   */
  public Slice<AcceptingConcertInfoResponse> findAcceptingConcertByAgent(UUID agentId, ConcertStatusFilteredRequest request) {
    Slice<ConcertAgentStatusInfo> infoSlice = concertAgentAvailabilityRepositoryCustom.findMyAcceptingConcert(agentId, request.toPageable());
    return infoSlice.map(availabilityMapper::toAcceptingConcertInfoResponse);
  }
}
