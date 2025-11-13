package com.ticketmate.backend.review.application.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewImgResponse {
  private UUID reviewImgId;

  private String reviewImgUrl;
}
