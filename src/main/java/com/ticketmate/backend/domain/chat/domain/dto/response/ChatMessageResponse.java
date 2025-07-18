package com.ticketmate.backend.domain.chat.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketmate.backend.domain.chat.domain.constant.ChatMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ChatMessageResponse {

  private String chatRoomId;
  private String messageId;
  private UUID senderId;
  private String senderNickname;
  private String message;  // 채팅 메시지 (텍스트)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime sendDate;
  @JsonIgnore
  private boolean read;  // 읽음 여부
  private String profileUrl;  // 프사
  private boolean mine;  // 메시지를 보낸 사람의 유무 (자신의 메시지이면 true/상대방의 메시지이면 false)
  private ChatMessageType chatMessageType;  // 채팅메시지 종류 (이미지 or 텍스트)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)  // 사진이 없는 텍스트 메시지면 제외
  private List<String> pictureMessageList;  // 사진 리스트

  @Builder
  public ChatMessageResponse(String chatRoomId, String messageId, UUID senderId, String senderNickname,
                             String message, LocalDateTime sendDate, boolean read, String profileUrl,
                             boolean mine, ChatMessageType chatMessageType, List<String> pictureMessageList) {
    this.chatRoomId = chatRoomId;
    this.messageId = messageId;
    this.senderId = senderId;
    this.senderNickname = senderNickname;
    this.message = message;
    this.sendDate = sendDate;
    this.read = read;
    this.profileUrl = profileUrl;
    this.mine = mine;
    this.chatMessageType = chatMessageType;
    this.pictureMessageList = pictureMessageList;
  }

  @JsonProperty("isRead")
  public boolean isRead() {
    return read;
  }
}
