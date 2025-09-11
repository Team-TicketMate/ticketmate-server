package com.ticketmate.backend.search.application.mapper;

import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.search.application.dto.response.AgentSearchResponse;
import com.ticketmate.backend.search.application.dto.response.ConcertSearchResponse;
import com.ticketmate.backend.search.application.dto.view.AgentSearchInfo;
import com.ticketmate.backend.search.application.dto.view.ConcertSearchInfo;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchMapperImpl implements SearchMapper {

  private final StorageService storageService;

  @Override
  public ConcertSearchResponse toConcertSearchResponse(ConcertSearchInfo info) {
    return new ConcertSearchResponse(
        info.concertId(),
        info.concertName(),
        info.concertHallName(),
        TimeUtil.toLocalDateTime(info.ticketPreOpenDate()),
        TimeUtil.toLocalDateTime(info.ticketGeneralOpenDate()),
        TimeUtil.toLocalDateTime(info.startDate()),
        TimeUtil.toLocalDateTime(info.endDate()),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.score()
    );
  }

  @Override
  public AgentSearchResponse toAgentSearchResponse(AgentSearchInfo info) {
    return new AgentSearchResponse(
        info.agentId(),
        info.nickname(),
        storageService.generatePublicUrl(info.profileImgStoredPath()),
        info.introduction(),
        info.averageRating(),
        info.reviewCount(),
        info.score()
    );
  }
}
