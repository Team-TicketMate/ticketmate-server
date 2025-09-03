package com.ticketmate.backend.chat.application.mapper;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomResponse;
import com.ticketmate.backend.chat.infrastructure.entity.ChatMessage;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.Map;
import java.util.UUID;

public interface ChatMapper {

  ChatMessageResponse toChatMessageResponse(ChatMessage message, UUID currentMemberId);

  ChatRoomResponse toChatRoomResponse(
      ChatRoom chatRoom,
      Member member,
      Map<UUID, ApplicationForm> applicationFormMap,
      Map<UUID, Member> mmemberMap,
      int unRead
  );
}
