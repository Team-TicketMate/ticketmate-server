package com.ticketmate.backend.chat.infrastructure.repository;

import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepositoryCustom {

  Page<ChatRoom> search(TicketOpenType ticketOpenType, String keyword, Member member, Integer pageNumber);
}
