package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentAcceptingConcertResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentConcertSettingResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.AgentConcertSettingInfo;

public interface ConcertAgentAvailabilityMapper {
  /**
   * ConcertAcceptingAgentInfo -> ConcertAcceptingAgentResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertAcceptingAgentResponse toConcertAcceptingAgentResponse(ConcertAcceptingAgentInfo info);

  /**
   * ConcertAgentStatusInfo -> AgentConcertSettingResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   * Integer (모집 중: 1, 모집 마감: 2) -> RecruitmentStatus Enum 변환
   */
  AgentConcertSettingResponse toConcertAgentStatusResponse(AgentConcertSettingInfo info);

  /**
   * ConcertAgentStatusInfo -> AgentAcceptingConcertResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  AgentAcceptingConcertResponse toAcceptingConcertInfoResponse(AgentConcertSettingInfo info);
}
