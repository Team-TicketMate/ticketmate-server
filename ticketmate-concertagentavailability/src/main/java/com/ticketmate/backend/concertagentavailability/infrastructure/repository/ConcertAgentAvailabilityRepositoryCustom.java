package com.ticketmate.backend.concertagentavailability.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.AgentConcertSettingInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ConcertAgentAvailabilityRepositoryCustom {

  // 특정 공연에 요청 수락 중인 정렬된 대리인 조회
  Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, Pageable pageable);

  // 대리인 마이페이지용 on/off 설정을 위한 전체 공연 조회
  Slice<AgentConcertSettingInfo> findMyConcertList(UUID agentId, Pageable pageable);

  // 대리인 마이페이지용 on 설정한 모집 중 공연 조회
  List<AgentConcertSettingInfo> findMyAcceptingConcert(UUID agentId);
}
