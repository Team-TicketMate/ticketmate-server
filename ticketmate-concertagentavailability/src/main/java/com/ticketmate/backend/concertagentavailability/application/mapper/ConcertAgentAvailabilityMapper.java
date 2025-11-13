package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentAcceptingConcertResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentConcertSettingResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.AgentConcertSettingInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;

public interface ConcertAgentAvailabilityMapper {
  /**
   * ConcertAcceptingAgentInfo -> ConcertAcceptingAgentResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertAcceptingAgentResponse toConcertAcceptingAgentResponse(ConcertAcceptingAgentInfo info);

  /**
   * AgentConcertSettingInfo -> AgentConcertSettingResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  AgentConcertSettingResponse toAgentConcertSettingResponse(AgentConcertSettingInfo info);

  /**
   * AgentConcertSettingInfo -> AgentAcceptingConcertResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  AgentAcceptingConcertResponse toAgentAcceptingConcertResponse(AgentConcertSettingInfo info);
}
