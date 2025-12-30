package com.ticketmate.backend.application.mapper;

import com.ticketmate.backend.application.dto.response.SuccessHistoryResponse;
import com.ticketmate.backend.core.constants.SuccessHistoryStatus;
import com.ticketmate.backend.infrastructure.repository.SuccessHistoryRow;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuccessHistoryMapperImpl implements SuccessHistoryMapper {

  private final StorageService storageService;

  @Override
  public SuccessHistoryResponse toSuccessHistoryResponse(SuccessHistoryRow successHistoryRow) {
    // 리뷰가 달렸는지 확인
    boolean reviewed = successHistoryRow.getReviewId() != null;

    return SuccessHistoryResponse.builder()
      .fulfillmentId(successHistoryRow.getFulfillmentId())
      .concertName(successHistoryRow.getConcertName())
      .concertThumbnailUrl(storageService.generatePublicUrl(successHistoryRow.getConcertThumbnailStoredPath()))
      .concertType(successHistoryRow.getConcertType())
      .createDate(successHistoryRow.getCreateDate())
      .successHistoryStatus(reviewed ? SuccessHistoryStatus.REVIEWED : SuccessHistoryStatus.NOT_REVIEWED)
      .reviewId(successHistoryRow.getReviewId())
      .reviewRating(successHistoryRow.getReviewRating())
      .clientNickname(successHistoryRow.getClientNickname())
      .build();
  }
}
