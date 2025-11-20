package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class FulfillmentFormMessageRequest implements ChatMessageRequest {

  private UUID fulfillmentFormId;
  private ChatMessageType chatMessageType;
  private String preview;
  private String rejectMemo;

  /**
   * 성공양식이 전송됐다는 메시지를 발송하기위한 플래그(티켓팅 성공했다는 뜻)
   */
  @Override
  public ChatMessageType getType() {
    return this.chatMessageType;
  }

  @Override
  public String toPreview() {
    return preview;
  }
}
