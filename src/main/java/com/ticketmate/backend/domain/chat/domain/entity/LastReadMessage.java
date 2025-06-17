package com.ticketmate.backend.domain.chat.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * 마지막으로 읽은 메시지의 포인터역할
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@RedisHash(value = "lastReadMessage", timeToLive = 2592000)
@Setter
public class LastReadMessage {

  @Id
  private String lastReadMessage; // "userLastRead:" + {roomId} + {memberId}
  private String lastMessageId;  // 마지막으로 **읽은** 메시지의 ID (마지막 메시지 X) [포인터용]
  @Indexed
  private String chatRoomId;
  @Indexed
  private UUID memberId;
  private LocalDateTime readDate;  // 읽은 시각

  @Builder
  public LastReadMessage(String lastReadMessage, String chatRoomId, UUID memberId,
      String lastMessageId, LocalDateTime readDate) {
    this.lastReadMessage = lastReadMessage;
    this.chatRoomId = chatRoomId;
    this.memberId = memberId;
    this.lastMessageId = lastMessageId;
    this.readDate = readDate;
  }

  // 새 포인터가 더 최신이면 갱신
  public void updatePointer(String newMessageId, LocalDateTime at) {
    if (this.lastMessageId == null || this.lastMessageId.compareTo(newMessageId) < 0) {
      this.lastMessageId = newMessageId;
      this.readDate = at;
    }
  }
}
