package com.ticketmate.backend.object.mongo.chat;

import com.ticketmate.backend.object.mongo.global.BaseMongoDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private UUID agentMemberId;  // 의뢰인 PK
    private UUID clientMemberId;  // 대리인 PK
    private UUID applicationFormId;  // 신청폼 PK
    private String roomName;  // 채팅방 이름
}
