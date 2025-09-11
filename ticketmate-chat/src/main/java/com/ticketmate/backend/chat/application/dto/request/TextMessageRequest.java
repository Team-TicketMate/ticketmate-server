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

  @NotBlank(message = "메시지를 입력해주세요.")
  @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다")
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
