package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.concert.core.constant.ConcertRecruitStatus;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentAcceptingConcertResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentConcertSettingResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.AgentConcertSettingInfo;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcertAgentAvailabilityMapperImpl implements ConcertAgentAvailabilityMapper {
  private final StorageService storageService;

  @Override
  public ConcertAcceptingAgentResponse toConcertAcceptingAgentResponse(ConcertAcceptingAgentInfo info) {
    return new ConcertAcceptingAgentResponse(
        info.agentId(),
        info.nickname(),
        storageService.generatePublicUrl(info.profileImgStoredPath()),
        info.introduction(),
        info.averageRating(),
        info.reviewCount()
    );
  }

  @Override
  public AgentConcertSettingResponse toConcertAgentStatusResponse(AgentConcertSettingInfo info) {
    return new AgentConcertSettingResponse(
        info.concertId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.status() == 1 ? ConcertRecruitStatus.OPEN : ConcertRecruitStatus.CLOSED,
        info.matchedClientCount(),
        info.accepting()
    );
  }

  @Override
  public AgentAcceptingConcertResponse toAcceptingConcertInfoResponse(AgentConcertSettingInfo info) {
    return new AgentAcceptingConcertResponse(
        info.concertId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.matchedClientCount()
    );
  }
}
