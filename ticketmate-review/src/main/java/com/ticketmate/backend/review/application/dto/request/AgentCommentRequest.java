package com.ticketmate.backend.review.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentCommentRequest {

  @NotBlank
  @NotBlankErrorCode(ErrorCode.COMMENT_EMPTY)
  @Size(max = 300)
  @SizeErrorCode(ErrorCode.COMMENT_LENGTH_INVALID)
  private String comment;
}
