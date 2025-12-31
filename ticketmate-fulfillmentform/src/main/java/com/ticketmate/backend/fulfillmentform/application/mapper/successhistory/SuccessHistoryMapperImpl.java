package com.ticketmate.backend.fulfillmentform.application.mapper.successhistory;

import com.ticketmate.backend.fulfillmentform.application.dto.successhistory.response.SuccessHistoryResponse;
import com.ticketmate.backend.fulfillmentform.core.constant.successhistory.SuccessHistoryStatus;
import com.ticketmate.backend.fulfillmentform.infrastructure.repository.successhistory.SuccessHistoryRow;
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
