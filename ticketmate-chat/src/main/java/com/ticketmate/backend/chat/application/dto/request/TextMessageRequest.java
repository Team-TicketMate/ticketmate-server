package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
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

  @NotBlank(message = "message가 비어있습니다")
  @Size(max = 500, message = "message는 최대 500자 입력 가능합니다")
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
