package com.ticketmate.backend.chat.infrastructure.repository;

import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {

  boolean existsByAgentMemberIdAndClientMemberIdAndConcertIdAndTicketOpenType(
      UUID agentId,
      UUID clientId,
      UUID concertId,
      TicketOpenType ticketOpenType
  );
}
