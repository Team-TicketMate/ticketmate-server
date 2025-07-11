package com.ticketmate.backend.domain.concert.repository;

import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ConcertAgentAvailabilityRepositoryCustom {
  public Slice<ConcertAcceptingAgentInfo> findAcceptingAgentByConcert(UUID concertId, Pageable pageable);
}
