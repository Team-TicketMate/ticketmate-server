package com.ticketmate.backend.chat.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadAckResponse {

  @Builder.Default
  private String type = "READ_ACK";

  private String chatRoomId;  // 채팅방 Id

  private UUID readerId;  // 메시지를 실제로 읽은 클라이언트의 PK [채팅방 입장]

  private UUID senderId;  // 원본 메시지를 쓴 사람의 PK

  private String lastReadMessageId;  // 마지막 메시지 Id

  private LocalDateTime readDate;
}
