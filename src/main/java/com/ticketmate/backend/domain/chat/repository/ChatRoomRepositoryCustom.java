package com.ticketmate.backend.domain.chat.repository;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatRoomListResponse;
import com.ticketmate.backend.domain.chat.domain.entity.ChatRoom;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
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
