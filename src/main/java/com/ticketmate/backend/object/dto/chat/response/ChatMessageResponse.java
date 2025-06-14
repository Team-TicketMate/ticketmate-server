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
    private String senderNickname;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime sendDate;
    private boolean isRead;  // 읽음 여부
    private String profileUrl;  // 프사

    @Builder
    public ChatMessageResponse(String chatRoomId, String messageId, UUID senderId, String senderNickname, String message, LocalDateTime sendDate, boolean isRead, String profileUrl) {
        this.chatRoomId = chatRoomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.message = message;
        this.sendDate = sendDate;
        this.isRead = isRead;
        this.profileUrl = profileUrl;
    }
}
