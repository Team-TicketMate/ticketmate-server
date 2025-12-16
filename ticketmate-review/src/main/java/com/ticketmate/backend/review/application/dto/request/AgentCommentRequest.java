package com.ticketmate.backend.review.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentCommentRequest {

  @NotBlank(message = "comment가 비어있습니다")
  @Size(max = 300, message = "comment는 최대 300자 입력 가능합니다")
  private String comment;
}
