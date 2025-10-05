package com.ticketmate.backend.review.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewInfoResponse {
  private UUID reviewId;

  private String concertTitle;

  private float rating;

  private String comment;

  private List<ReviewImgResponse> reviewImgList;

  private LocalDateTime createdDate;

  private AgentCommentResponse agentComment;
}
