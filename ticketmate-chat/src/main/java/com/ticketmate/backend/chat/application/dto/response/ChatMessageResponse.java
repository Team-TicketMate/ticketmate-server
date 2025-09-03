package com.ticketmate.backend.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

  private String chatRoomId;

  private String messageId;

  private UUID senderId;

  private String senderNickname;

  private String message;  // 채팅 메시지 (텍스트)

  private LocalDateTime sendDate;

  @JsonIgnore
  private boolean read;  // 읽음 여부

  private String profileUrl;  // 프사

  private boolean mine;  // 메시지를 보낸 사람의 유무 (자신의 메시지이면 true/상대방의 메시지이면 false)

  private ChatMessageType chatMessageType;  // 채팅메시지 종류 (이미지 or 텍스트)

  @JsonInclude(JsonInclude.Include.NON_EMPTY)  // 사진이 없는 텍스트 메시지면 제외
  private List<String> pictureMessageUrlList;  // 사진 URL 리스트

  @JsonProperty("isRead")
  public boolean isRead() {
    return read;
  }
}
