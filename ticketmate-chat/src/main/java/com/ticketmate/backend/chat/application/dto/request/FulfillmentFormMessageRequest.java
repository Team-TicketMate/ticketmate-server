package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class FulfillmentFormMessageRequest implements ChatMessageRequest {

  private final UUID fulfillmentFormId;
  private final ChatMessageType chatMessageType;
  private final String preview;
  private final String rejectMemo;


  @Builder
  public FulfillmentFormMessageRequest(UUID fulfillmentFormId, ChatMessageType chatMessageType, String preview, String rejectMemo) {
    this.fulfillmentFormId = fulfillmentFormId;
    this.chatMessageType = chatMessageType;
    this.preview = preview;
    this.rejectMemo = rejectMemo;
  }

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
