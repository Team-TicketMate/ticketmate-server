package com.ticketmate.backend.concert.application.mapper;

import com.ticketmate.backend.concert.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertFilteredInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public interface ConcertMapper {

  /**
   * ConcertInfo -> ConcertInfoResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertInfoResponse toConcertInfoResponse(ConcertInfo info);

  /**
   * ConcertFilteredInfo -> ConcertFilteredResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertFilteredResponse toConcertFilteredResponse(ConcertFilteredInfo info);

  /**
   * Page<ConcertFilteredInfo> -> Page<CocnertFilteredResponse> (DTO Page -> DTO Page)
   * 이미지 storedPath -> publicUrl 변환
   */
  Page<ConcertFilteredResponse> toConcertFilteredResponsePage(Page<ConcertFilteredInfo> infoPage);

  /**
   * ConcertAcceptingAgentInfo -> ConcertAcceptingAgentResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  ConcertAcceptingAgentResponse toConcertAcceptingAgentResponse(ConcertAcceptingAgentInfo info);

  /**
   * Slice<ConcertAcceptingAgentInfo> -> Slice<ConcertAcceptingAgentResponse> (DTO Slice -> DTO Slice)
   * 이미지 storedPath -> publicUrl 변환
   */
  Slice<ConcertAcceptingAgentResponse> toConcertAcceptingAgentResponseSlice(Slice<ConcertAcceptingAgentInfo> infoSlice);
}
