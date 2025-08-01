package com.ticketmate.backend.chat.infrastructure.repository;

import com.ticketmate.backend.chat.infrastructure.entity.ChatMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryCustomImpl implements ChatMessageRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public long markReadUpTo(String chatRoomId, UUID readerId) {

    Query query = new Query(
        Criteria.where("chatRoomId").is(chatRoomId)
            .and("senderId").ne(readerId)  // 상대가 보낸 내 메시지
            .and("isRead").is(false));  // 아직 안 읽힌 것만
    Update update = Update.update("isRead", true);

    return mongoTemplate.updateMulti(query, update, ChatMessage.class)
        .getModifiedCount();
  }

}
