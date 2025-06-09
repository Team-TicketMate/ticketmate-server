package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.chat.response.ChatMessageResponse;
import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class MongoMapper {
    public ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSenderId())
                .chatRoomId(message.getRoomId())
                .sender(message.getSenderNickName())
                .message(message.getMessage())
                .profileImageUrl(message.getSenderProfileImg())
                .isRead(message.isRead())
                .sendDate(message.getSendDate())
                .build();
    }
}

