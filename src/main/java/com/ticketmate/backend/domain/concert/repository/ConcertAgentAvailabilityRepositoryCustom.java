package com.ticketmate.backend.domain.concert.repository;

import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ConcertAgentAvailabilityRepositoryCustom {
  // 특정 공연에 요청 수락 중인 정렬된 대리인 조회
  public Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, Pageable pageable);
}
