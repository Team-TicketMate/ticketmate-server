package com.ticketmate.backend.chat.infrastructure.entity;

import com.ticketmate.backend.chat.core.constant.ChatMessageType;
import com.ticketmate.backend.common.infrastructure.persistence.BaseMongoDocument;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseMongoDocument {

  @Id
  private String chatMessageId; // MongoDB 식별자

  @Indexed
  private String chatRoomId; // 채팅방 PK (String/ObjectId)

  @Indexed
  private UUID senderId; // 메시지 보낸 유저 식별자 (UUID 등)

  private String senderNickName; // 메시지 보낸 유저 닉네임

  private String senderEmail; // 메시지 보낸 유저 이메일

  private String senderProfileImgStoredPath; // 메시지 보낸 유저 프로필 사진

  private String message; // 텍스트 전용 메시지 내용

  private boolean isRead; // 읽음 여부 (필요시 세분화 가능)

  private Instant sendDate; // 전송 시간

  private ChatMessageType chatMessageType; // 채팅 종류 (텍스트 or 이미지)

  @Builder.Default
  private List<String> pictureMessageStoredPathList = new ArrayList<>(); // 사진 전용 메시지 리스트

  private String referenceId;   // 성공양식같은 특별한 메시지를 위한 필드(클릭 시 성공양식 상세로 이동하기 간편하게 하기위해 추가)
}