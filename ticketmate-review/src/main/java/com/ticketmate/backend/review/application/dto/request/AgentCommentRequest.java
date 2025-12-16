package com.ticketmate.backend.review.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentCommentRequest {

  @NotBlank(message = "comment가 비어있습니다")
  @Size(max = 300, message = "댓글 내용은 300자 이하로 작성해야 합니다.")
  private String comment;
}
