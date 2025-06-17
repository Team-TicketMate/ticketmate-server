package com.ticketmate.backend.domain.chat.domain.entity;

import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.global.BaseMongoDocument;
import java.time.LocalDateTime;
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

@Document(collection = "chat_room")
// 대리인, 의뢰인, 콘서트, 선예매/일반예매 4가지 필드의 복합 인덱스 (4가지 조건이 갖춰진 데이터는 1개여야함)
@CompoundIndex(
    name = "uk_agent_client_concert_preopen",
    def = "{'agentMemberId': 1, "
          + "'clientMemberId': 1, "
          + "'concertId': 1, "
          + "'preOpen': 1}",
    unique = true
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Setter
@Getter
public class ChatRoom extends BaseMongoDocument {

  @Id
  private String chatRoomId;  // 채팅방 PK
  @Indexed
  private UUID agentMemberId;  // 대리인 PK
  @Indexed
  private UUID clientMemberId;  // 의뢰인 PK
  @Indexed
  private String agentMemberNickname;  // 대리인 닉네임 (채팅방 이름 파싱용)
  @Indexed
  private String clientMemberNickname;  // 의뢰인 닉네임 (채팅방 이름 파싱용)
  @Indexed
  private UUID applicationFormId;  // 신청폼 PK
  @Indexed
  private LocalDateTime lastMessageTime;  // 마지막 메시지가 온 시간
  @Indexed
  private String lastMessage;  // 마지막 메시지
  @Indexed
  private UUID concertId;  // 콘서트
  @Indexed
  private String lastMessageId;  // 마지막 메시지의 ID 정보
  private TicketOpenType ticketOpenType;  // 신청폼의 선예매, 일반예매인지

  public void updateLastMessage(String message) {
    this.lastMessage = message;
  }

  public void updateLastMessageTime(LocalDateTime lastMessageTime) {
    this.lastMessageTime = lastMessageTime;
  }

  public void updateLastMessageId(String messageId) {
    this.lastMessageId = messageId;
  }
}
