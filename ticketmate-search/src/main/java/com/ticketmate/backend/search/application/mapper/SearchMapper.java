package com.ticketmate.backend.search.application.mapper;

import com.ticketmate.backend.search.application.dto.response.AgentSearchResponse;
import com.ticketmate.backend.search.application.dto.response.ConcertSearchResponse;
import com.ticketmate.backend.search.application.dto.view.AgentSearchInfo;
import com.ticketmate.backend.search.application.dto.view.ConcertSearchInfo;

public interface SearchMapper {

  /**
   * ConcertSearchInfo -> ConcertSearchResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertSearchResponse toConcertSearchResponse(ConcertSearchInfo info);

  /**
   * AgentSearchInfo -> AgentSearchResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  AgentSearchResponse toAgentSearchResponse(AgentSearchInfo info);
}
