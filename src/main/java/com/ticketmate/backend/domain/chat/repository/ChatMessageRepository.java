package com.ticketmate.backend.domain.chat.repository;

import com.ticketmate.backend.domain.chat.domain.entity.ChatMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageRepositoryCustom {

  List<ChatMessage> findByChatRoomIdOrderBySendDateAsc(String chatRoomId);
}
