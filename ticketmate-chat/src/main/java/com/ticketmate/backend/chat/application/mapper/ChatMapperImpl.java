package com.ticketmate.backend.chat.application.mapper;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.chat.application.dto.response.ChatMessageResponse;
import com.ticketmate.backend.chat.application.dto.response.ChatRoomResponse;
import com.ticketmate.backend.chat.application.service.ChatRoomService;
import com.ticketmate.backend.chat.infrastructure.entity.ChatMessage;
import com.ticketmate.backend.chat.infrastructure.entity.ChatRoom;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapperImpl implements ChatMapper {

  private final ChatMapStruct mapStruct;
  private final StorageService storageService;

  @Override
  public ChatMessageResponse toChatMessageResponse(ChatMessage message, UUID currentMemberId) {
    String profileImgStoredPath = message.getSenderProfileImgStoredPath();
    List<String> pictureMessageUrlList = message.getPictureMessageStoredPathList().stream()
        .map(storageService::generatePublicUrl)
        .toList();

    return ChatMessageResponse.builder()
        .chatRoomId(message.getChatRoomId())
        .messageId(message.getChatMessageId())
        .senderId(message.getSenderId())
        .senderNickname(message.getSenderNickName())
        .message(message.getMessage())
        .sendDate(message.getSendDate())
        .read(message.isRead())
        .profileUrl(storageService.generatePublicUrl(profileImgStoredPath))
        .mine(message.getSenderId().equals(currentMemberId))
        .chatMessageType(message.getChatMessageType())
        .pictureMessageUrlList(pictureMessageUrlList)
        .build();
  }

  @Override
  public ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom, Member member, Map<UUID, ApplicationForm> applicationFormMap, Map<UUID, Member> mmemberMap, int unRead) {
    // 매핑을 위한 값 세팅
    UUID opponentId = ChatRoomService.opponentIdOf(chatRoom, member);
    Member other = mmemberMap.get(opponentId);
    ApplicationForm applicationForm = applicationFormMap.get(chatRoom.getApplicationFormId());
    String concertThumbnailStoredPath = applicationForm.getConcert().getConcertThumbnailStoredPath();
    String profileImgStoredPath = other.getProfileImgStoredPath();

    return ChatRoomResponse.builder()
        .unReadMessageCount(unRead)
        .chatRoomId(chatRoom.getChatRoomId())
        .chatRoomName(other.getNickname())  // 상대방 닉네임 출력
        .ticketOpenType(chatRoom.getTicketOpenType())
        .lastChatMessage(chatRoom.getLastMessage())
        .concertThumbnailUrl(storageService.generatePublicUrl(concertThumbnailStoredPath))
        .lastChatSendTime(chatRoom.getLastMessageTime())
        .profileUrl(storageService.generatePublicUrl(profileImgStoredPath))
        .build();
  }
}
