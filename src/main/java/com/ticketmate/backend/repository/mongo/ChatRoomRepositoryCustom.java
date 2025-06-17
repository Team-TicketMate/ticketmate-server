package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.dto.chat.response.ChatRoomListResponse;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepositoryCustom {

  Page<ChatRoom> search(TicketOpenType ticketOpenType, String keyword, Member member, Integer pageNumber);

  ChatRoomListResponse toResponse(ChatRoom room, Member member,
      Map<UUID, ApplicationForm> applicationFormMap,
      Map<UUID, Member> memberMap,
      int unRead);
}
