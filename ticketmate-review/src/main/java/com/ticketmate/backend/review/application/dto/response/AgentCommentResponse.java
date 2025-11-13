package com.ticketmate.backend.review.application.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentCommentResponse {
  private String agentNickname;

  private String agentProfileUrl;

  private String comment;

  private LocalDateTime commentedDate;
}
