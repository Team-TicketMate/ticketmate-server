package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageRepositoryCustom {
    List<ChatMessage> findByRoomIdOrderBySendDateAsc(String roomId);
}
