package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.chat.response.ChatMessageResponse;
import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class MongoMapper {
    public ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getChatMessageId())
                .senderId(message.getSenderId())
                .chatRoomId(message.getChatRoomId())
                .senderNickname(message.getSenderNickName())
                .message(message.getMessage())
                .profileUrl(message.getSenderProfileUrl())
                .isRead(message.isRead())
                .sendDate(message.getSendDate())
                .build();
    }
}

