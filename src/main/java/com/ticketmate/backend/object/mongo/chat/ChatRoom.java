package com.ticketmate.backend.object.mongo.chat;

import com.ticketmate.backend.object.mongo.global.BaseMongoDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Setter
@Getter
public class ChatRoom extends BaseMongoDocument {
    @Id
    private String roomId;  // 채팅방 PK
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
    private Boolean preOpen;  // 신청폼의 선예매, 일반예매인지
}
