package com.ticketmate.backend.domain.chat.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
  private boolean mine;  // 메시지를 보낸 사람의 유무 (자신의 메시지이면 true/상대방의 메시지이면 false)

  @Builder
  public ChatMessageResponse(String chatRoomId, String messageId, UUID senderId, String senderNickname,
                             String message, LocalDateTime sendDate, boolean read, String profileUrl, boolean mine) {
    this.chatRoomId = chatRoomId;
    this.messageId = messageId;
    this.senderId = senderId;
    this.senderNickname = senderNickname;
    this.message = message;
    this.sendDate = sendDate;
    this.read = read;
    this.profileUrl = profileUrl;
    this.mine = mine;
  }

  @JsonProperty("isRead")
  public boolean isRead() {
    return read;
  }
}
