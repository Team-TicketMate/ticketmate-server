package com.ticketmate.backend.domain.chat.repository;

import com.ticketmate.backend.domain.chat.domain.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageRepositoryCustom {
  Slice<ChatMessage> findByChatRoomId(String chatRoomId, Pageable pageable);
}
