package com.ticketmate.backend.fulfillmentform.application.dto.successhistory.response;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.fulfillmentform.core.constant.successhistory.SuccessHistoryStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessHistoryResponse {

  private UUID fulfillmentId;
  private String concertName;
  private String concertThumbnailUrl;
  private ConcertType concertType;
  private LocalDateTime createDate;
  private SuccessHistoryStatus successHistoryStatus;
  private UUID reviewId;
  private Float reviewRating;
  private String clientNickname;
}
