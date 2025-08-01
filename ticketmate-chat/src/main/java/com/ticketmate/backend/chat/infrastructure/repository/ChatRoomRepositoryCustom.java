package com.ticketmate.backend.chat.infrastructure.repository;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomListResponse;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
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
