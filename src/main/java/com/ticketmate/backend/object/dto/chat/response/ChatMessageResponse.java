package com.ticketmate.backend.object.dto.chat.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String sender;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime sendDate;
    private boolean isRead;  // 읽음 여부
    private String profileImageUrl;  // 프사

    @Builder
    public ChatMessageResponse(String chatRoomId, String messageId, UUID senderId, String sender, String message, LocalDateTime sendDate, boolean isRead, String profileImageUrl) {
        this.chatRoomId = chatRoomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.sender = sender;
        this.message = message;
        this.sendDate = sendDate;
        this.isRead = isRead;
        this.profileImageUrl = profileImageUrl;
    }
}
