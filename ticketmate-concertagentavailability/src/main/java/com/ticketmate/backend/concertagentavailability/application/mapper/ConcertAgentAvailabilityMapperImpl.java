package com.ticketmate.backend.concertagentavailability.application.mapper;

import com.ticketmate.backend.concert.core.constant.RecruitmentStatus;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AcceptingConcertInfoResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAgentStatusResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concertagentavailability.application.dto.view.ConcertAgentStatusInfo;
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
  public ConcertAgentStatusResponse toConcertAgentStatusResponse(ConcertAgentStatusInfo info) {
    return new ConcertAgentStatusResponse(
        info.concertId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.status() == 1 ? RecruitmentStatus.OPEN : RecruitmentStatus.CLOSED,
        info.matchedClientCount(),
        info.accepting()
    );
  }

  @Override
  public AcceptingConcertInfoResponse toAcceptingConcertInfoResponse(ConcertAgentStatusInfo info) {
    return new AcceptingConcertInfoResponse(
        info.concertId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.matchedClientCount()
    );
  }
}
