package com.ticketmate.backend.chat.infrastructure.repository;

import com.ticketmate.backend.chat.infrastructure.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String>, ChatMessageRepositoryCustom {

  Slice<ChatMessage> findByChatRoomId(String chatRoomId, Pageable pageable);
}
