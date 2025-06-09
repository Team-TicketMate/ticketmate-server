package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {
    boolean existsByAgentMemberIdAndClientMemberIdAndConcertIdAndPreOpen(UUID agentId, UUID clientId
            , UUID concertId, TicketOpenType ticketOpenType);
}
