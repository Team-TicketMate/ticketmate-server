package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {

  boolean existsByAgentMemberIdAndClientMemberIdAndConcertIdAndTicketOpenType(UUID agentId, UUID clientId
      , UUID concertId, TicketOpenType ticketOpenType);
}
