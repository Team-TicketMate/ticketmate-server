package com.ticketmate.backend.concert.infrastructure.repository;

import com.ticketmate.backend.concert.application.dto.response.ConcertAcceptingAgentResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ConcertAgentAvailabilityRepositoryCustom {

  // 특정 공연에 요청 수락 중인 정렬된 대리인 조회
  public Slice<ConcertAcceptingAgentResponse> findAcceptingAgentByConcert(UUID concertId, Pageable pageable);
}
