package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {
    Optional<ChatRoom> findByAgentMemberIdAndClientMemberId(UUID agentId, UUID clientId);
}
