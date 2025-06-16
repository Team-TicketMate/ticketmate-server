package com.ticketmate.backend.object.dto.chat.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReadAckResponse {
    @Builder.Default
    private String type = "READ_ACK";
    private String chatRoomId;  // 채팅방 Id
    private UUID readerId;  // 메시지를 실제로 읽은 클라이언트의 PK [채팅방 입장]
    private UUID senderId;  // 원본 메시지를 쓴 사람의 PK
    private String lastReadMessageId;  // 마지막 메시지 Id
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime readDate;
}
