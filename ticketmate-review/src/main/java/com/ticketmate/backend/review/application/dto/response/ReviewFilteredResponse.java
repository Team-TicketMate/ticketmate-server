package com.ticketmate.backend.review.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewFilteredResponse {
  private UUID reviewId;

  private String concertTitle;

  private float rating;

  private String comment;

  private List<ReviewImgResponse> reviewImgList;

  private LocalDateTime createdDate;

  // TODO: UI 확정되면 수정
}
