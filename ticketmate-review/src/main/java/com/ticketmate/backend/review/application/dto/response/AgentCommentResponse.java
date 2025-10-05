package com.ticketmate.backend.review.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AgentCommentResponse {
  private String agentNickname;

  private String agentProfileUrl;

  private String comment;

  private LocalDateTime commentedDate;
}
