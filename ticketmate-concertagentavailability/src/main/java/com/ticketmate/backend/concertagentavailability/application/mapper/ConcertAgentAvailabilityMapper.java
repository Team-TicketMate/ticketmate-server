package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.concertagentavailability.application.dto.response.AcceptingConcertInfoResponse;
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

  /**
   * ConcertAgentStatusInfo -> ConcertAgentStatusResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   * Integer (모집 중: 1, 모집 마감: 2) -> RecruitmentStatus Enum 변환
   */
  ConcertAgentStatusResponse toConcertAgentStatusResponse(ConcertAgentStatusInfo info);

  /**
   * ConcertAgentStatusInfo -> AcceptingConcertInfoResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  AcceptingConcertInfoResponse toAcceptingConcertInfoResponse(ConcertAgentStatusInfo info);
}
