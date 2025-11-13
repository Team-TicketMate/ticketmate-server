package com.ticketmate.backend.chat.infrastructure.entity;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import com.ticketmate.backend.common.infrastructure.persistence.BaseMongoDocument;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 대리인, 의뢰인, 콘서트, 선예매/일반예매 4가지 필드의 복합 인덱스 (4가지 조건이 갖춰진 데이터는 1개여야함)
@CompoundIndex(
  name = "uk_agent_client_concert_ticketOpenType",
  def = "{'agentMemberId': 1, "
        + "'clientMemberId': 1, "
        + "'concertId': 1, "
        + "'ticketOpenType': 1}",
  unique = true
)
public class ChatRoom extends BaseMongoDocument {

  @Id
  private String chatRoomId; // 채팅방 PK

  @Indexed
  private UUID agentMemberId; // 대리인 PK

  @Indexed
  private UUID clientMemberId; // 의뢰인 PK

  @Indexed
  private String agentMemberNickname; // 대리인 닉네임 (채팅방 이름 파싱용)

  @Indexed
  private String clientMemberNickname; // 의뢰인 닉네임 (채팅방 이름 파싱용)

  @Indexed
  private UUID applicationFormId; // 신청폼 PK

  @Indexed
  private Instant lastMessageTime; // 마지막 메시지가 온 시간

  @Indexed
  private String lastMessage; // 마지막 메시지

  @Indexed
  private UUID concertId; // 콘서트

  @Indexed
  private String lastMessageId; // 마지막 메시지의 ID 정보

  @Indexed
  private ChatMessageType lastMessageType; // 마지막 메시지의 타입 (텍스트, 사진)

  private TicketOpenType ticketOpenType; // 신청폼의 선예매, 일반예매인지

  public void updateLastMessage(String message) {
    this.lastMessage = message;
  }

  public void updateLastMessageTime(Instant lastMessageTime) {
    this.lastMessageTime = lastMessageTime;
  }

  public void updateLastMessageId(String messageId) {
    this.lastMessageId = messageId;
  }

  public void updateLastMessageType(ChatMessageType chatMessageType) {
    this.lastMessageType = chatMessageType;
  }

  // 상대방 아이디 추출
  public UUID getOpponentId(UUID currentMemberId) {
    return currentMemberId.equals(agentMemberId) ? clientMemberId : agentMemberId;
  }

  // 상대방 닉네임 추출
  public String getOpponentNickname(UUID currentMemberId) {
    return currentMemberId.equals(agentMemberId) ? clientMemberNickname : agentMemberNickname;
  }
}
