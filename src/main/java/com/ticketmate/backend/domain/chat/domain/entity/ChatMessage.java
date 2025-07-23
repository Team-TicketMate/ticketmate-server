package com.ticketmate.backend.domain.chat.domain.entity;

import com.ticketmate.backend.domain.chat.domain.constant.ChatMessageType;
import com.ticketmate.backend.global.BaseMongoDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AllArgsConstructor
@Document
@Setter
public class ChatMessage extends BaseMongoDocument {

  @Id
  private String chatMessageId;      // MongoDB 식별자
  @Indexed
  private String chatRoomId;         // 채팅방 PK (String/ObjectId)
  @Indexed
  private UUID senderId;         // 메시지 보낸 유저 식별자 (UUID 등)
  private String senderNickName;  // 메시지 보낸 유저 닉네임
  private String senderEmail;         // 메시지 보낸 유저 이메일
  private String senderProfileUrl;  // 메시지 보낸 유저 프로필 사진
  private String message;        // 텍스트 전용 메시지 내용
  private boolean isRead;        // 읽음 여부 (필요시 세분화 가능)
  private LocalDateTime sendDate;  // 전송 시간
  private ChatMessageType chatMessageType;  // 채팅 종류 (텍스트 or 이미지)
  @Builder.Default
  private List<String> pictureMessageList = new ArrayList<>();  // 사진 전용 메시지 리스트
}