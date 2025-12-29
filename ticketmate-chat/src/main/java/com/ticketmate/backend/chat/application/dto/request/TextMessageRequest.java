package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class TextMessageRequest implements ChatMessageRequest {

  @NotBlank
  @NotBlankErrorCode(ErrorCode.MESSAGE_EMPTY)
  @Size(max = 500)
  @SizeErrorCode(ErrorCode.MESSAGE_TOO_LONG)
  private String message;

  @Override
  public ChatMessageType getType() {
    return ChatMessageType.TEXT;
  }

  @Override
  public String toPreview() {
    return message;
  }
}
