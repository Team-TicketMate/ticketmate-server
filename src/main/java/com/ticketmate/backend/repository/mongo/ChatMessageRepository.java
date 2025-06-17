package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageRepositoryCustom {

  List<ChatMessage> findByChatRoomIdOrderBySendDateAsc(String chatRoomId);
}
