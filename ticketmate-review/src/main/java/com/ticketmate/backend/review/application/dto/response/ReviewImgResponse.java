package com.ticketmate.backend.review.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewImgResponse {
  private UUID reviewImgId;

  private String reviewImgUrl;
}
