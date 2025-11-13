package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentAcceptingConcertResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentConcertSettingResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.AgentConcertSettingInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcertAgentAvailabilityMapperImpl implements ConcertAgentAvailabilityMapper {
  private final StorageService storageService;

  @Override
  public ConcertAcceptingAgentResponse toConcertAcceptingAgentResponse(ConcertAcceptingAgentInfo info) {
    String profileImgStoredPath = null;
    if (!CommonUtil.nvl(info.profileImgStoredPath(), "").isEmpty()) {
      profileImgStoredPath = storageService.generatePublicUrl(info.profileImgStoredPath());
    }

    return new ConcertAcceptingAgentResponse(
        info.agentId(),
        info.nickname(),
        profileImgStoredPath,
        info.introduction(),
        info.averageRating(),
        info.reviewCount()
    );
  }

  @Override
  public AgentConcertSettingResponse toAgentConcertSettingResponse(AgentConcertSettingInfo info) {
    return new AgentConcertSettingResponse(
        info.concertId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.matchedClientCount(),
        info.accepting()
    );
  }

  @Override
  public AgentAcceptingConcertResponse toAgentAcceptingConcertResponse(AgentConcertSettingInfo info) {
    return new AgentAcceptingConcertResponse(
        info.concertId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.matchedClientCount()
    );
  }
}
