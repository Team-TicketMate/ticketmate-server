package com.ticketmate.backend.chat.application.mapper;

import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.infrastructure.entity.ChatMessage;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMapper {

  /**
   * ChatMessage(Mongo) → ChatMessageResponse(DTO)
   */
  @Mapping(source = "message.chatMessageId", target = "messageId")
  @Mapping(source = "message.senderNickName", target = "senderNickname")
  @Mapping(source = "message.senderProfileUrl", target = "profileUrl")
  @Mapping(target = "mine", expression = "java(message.getSenderId().equals(currentMemberId))")
  ChatMessageResponse toChatMessageResponse(ChatMessage message, UUID currentMemberId);
}
