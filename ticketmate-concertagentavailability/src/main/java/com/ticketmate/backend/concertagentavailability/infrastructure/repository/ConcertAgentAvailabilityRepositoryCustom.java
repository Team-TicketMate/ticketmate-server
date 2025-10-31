package com.ticketmate.backend.concertagentavailability.infrastructure.repository;

import java.util.UUID;

import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAgentStatusResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAgentStatusInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ConcertAgentAvailabilityRepositoryCustom {

  // 특정 공연에 요청 수락 중인 정렬된 대리인 조회
  Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, Pageable pageable);
}
