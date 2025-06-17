package com.ticketmate.backend.domain.chat.repository;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {

  boolean existsByAgentMemberIdAndClientMemberIdAndConcertIdAndTicketOpenType(UUID agentId, UUID clientId
      , UUID concertId, TicketOpenType ticketOpenType);
}
