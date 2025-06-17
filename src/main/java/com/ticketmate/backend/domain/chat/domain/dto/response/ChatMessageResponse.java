package com.ticketmate.backend.domain.chat.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageResponse {

  private String chatRoomId;
  private String messageId;
  private UUID senderId;
  private String senderNickname;
  private String message;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime sendDate;
  @JsonIgnore
  private boolean read;  // 읽음 여부
  private String profileUrl;  // 프사

  @Builder
  public ChatMessageResponse(String chatRoomId, String messageId, UUID senderId, String senderNickname, String message, LocalDateTime sendDate, boolean read, String profileUrl) {
    this.chatRoomId = chatRoomId;
    this.messageId = messageId;
    this.senderId = senderId;
    this.senderNickname = senderNickname;
    this.message = message;
    this.sendDate = sendDate;
    this.read = read;
    this.profileUrl = profileUrl;
  }

  @JsonProperty("isRead")
  public boolean isRead() {
    return read;
  }
}
