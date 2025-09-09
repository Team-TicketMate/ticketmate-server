package com.ticketmate.backend.concert.application.mapper;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.concert.application.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.concert.application.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.concert.application.dto.view.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertFilteredInfo;
import com.ticketmate.backend.concert.application.dto.view.ConcertInfo;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcertMapperImpl implements ConcertMapper {

  private final ConcertMapStruct mapStruct;
  private final StorageService storageService;

  @Override
  public ConcertInfoResponse toConcertInfoResponse(ConcertInfo info) {
    String concertThumbnailUrl = storageService.generatePublicUrl(info.concertThumbnailStoredPath());
    String seatingChartUrl = null;
    if (!CommonUtil.nvl(info.seatingChartStoredPath(), "").isEmpty()) {
      seatingChartUrl = storageService.generatePublicUrl(info.seatingChartStoredPath());
    }
    return new ConcertInfoResponse(
        info.concertName(),
        info.concertHallName(),
        concertThumbnailUrl,
        seatingChartUrl,
        info.concertType(),
        info.concertDateInfoResponseList(),
        info.ticketOpenDateInfoResponseList(),
        info.ticketReservationSite()
    );
  }

  @Override
  public ConcertFilteredResponse toConcertFilteredResponse(ConcertFilteredInfo info) {
    String concertThumbnailUrl = storageService.generatePublicUrl(info.concertThumbnailStoredPath());
    String seatingChartUrl = null;
    if (!CommonUtil.nvl(info.seatingChartStoredPath(), "").isEmpty()) {
      seatingChartUrl = storageService.generatePublicUrl(info.seatingChartStoredPath());
    }
    return new ConcertFilteredResponse(
        info.concertId(),
        info.concertName(),
        info.concertHallName(),
        info.concertType(),
        info.ticketReservationSite(),
        TimeUtil.toLocalDateTime(info.ticketPreOpenDate()),
        info.preOpenBankTransfer(),
        TimeUtil.toLocalDateTime(info.ticketGeneralOpenDate()),
        info.generalOpenBankTransfer(),
        TimeUtil.toLocalDateTime(info.startDate()),
        TimeUtil.toLocalDateTime(info.endDate()),
        concertThumbnailUrl,
        seatingChartUrl
    );
  }

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
}
