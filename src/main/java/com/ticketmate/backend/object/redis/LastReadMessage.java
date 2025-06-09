package com.ticketmate.backend.object.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 마지막으로 읽은 메시지의 포인터역할
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@RedisHash(value = "lastReadMessage", timeToLive = 2592000)
@Setter
public class LastReadMessage {
    @Id
    private String id; // "userLastRead:" + {roomId} + {memberId}
    private String lastMessageId;  // 마지막으로 **읽은** 메시지의 ID (마지막 메시지 X) [포인터용]
    @Indexed
    private String roomId;
    @Indexed
    private UUID memberId;
    private LocalDateTime readAt;  // 읽은 시각

    @Builder
    public LastReadMessage(String roomId, UUID memberId,
                           String lastMessageId, LocalDateTime readAt) {
        this.id = "userLastRead:%s:%s".formatted(roomId, memberId);
        this.roomId = roomId;
        this.memberId = memberId;
        this.lastMessageId = lastMessageId;
        this.readAt = readAt;
    }

    /** 새 포인터가 더 최신이면 갱신 */
    public void updatePointer(String newMsgId, LocalDateTime at) {
        if (this.lastMessageId == null ||
                this.lastMessageId.compareTo(newMsgId) < 0) {
            this.lastMessageId = newMsgId;
            this.readAt = at;
        }
    }
}
