package com.ticketmate.backend.object.mongo.chat;

import com.ticketmate.backend.object.mongo.global.BaseMongoDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AllArgsConstructor
@Document(collection = "chat_message")
@Setter
public class ChatMessage extends BaseMongoDocument {

    public enum MessageType {
        ENTER, TALK
    }

    @Id
    private String messageId;      // MongoDB 식별자
    @Indexed
    private String roomId;         // 채팅방 PK (String/ObjectId)
    @Indexed
    private UUID senderId;         // 메시지 보낸 유저 식별자 (UUID 등)
    @Indexed
    private String senderNickName;  // 메시지 보낸 유저 닉네임
    @Indexed
    private String senderEmail;         // 메시지 보낸 유저 이메일
    @Indexed
    private String senderProfileImg;  // 메시지 보낸 유저 프로필 사진
    private String message;        // 메시지 내용
    private boolean isRead;        // 읽음 여부 (필요시 세분화 가능)
    private MessageType messageType;  // ENTER, TALK 등
    private LocalDateTime sendDate;  // 전송 시간
}