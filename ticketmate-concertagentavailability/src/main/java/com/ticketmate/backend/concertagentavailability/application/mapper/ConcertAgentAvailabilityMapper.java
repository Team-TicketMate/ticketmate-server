package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAgentStatusResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAgentStatusInfo;

public interface ConcertAgentAvailabilityMapper {
  /**
   * ConcertAcceptingAgentInfo -> ConcertAcceptingAgentResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertAcceptingAgentResponse toConcertAcceptingAgentResponse(ConcertAcceptingAgentInfo info);
}
