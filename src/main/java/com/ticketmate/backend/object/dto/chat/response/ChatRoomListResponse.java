package com.ticketmate.backend.object.dto.chat.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.TicketOpenType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomListResponse {
    private String chatRoomId;  // 채팅방 PK
    private String chatRoomName;  // 상대방 닉네임 출력
    private String lastChatMessage;  // 마지막 채팅 메시지
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastChatSendTime;  // 마지막으로 채팅된 시간
    private String profileUrl; // 상대방 프로필 사진
    private String concertThumbnailUrl; // 콘서트 썸네일 사진
    private TicketOpenType ticketOpenType;  // 선예매/일예 구분
    private int unReadMessageCount;  // 읽지 않은 메시지

    @Builder
    public ChatRoomListResponse(String chatRoomId, String chatRoomName, String lastChatMessage, LocalDateTime lastChatSendTime, String profileUrl, String concertThumbnailUrl, TicketOpenType ticketOpenType, int unReadMessageCount) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.lastChatMessage = lastChatMessage;
        this.lastChatSendTime = lastChatSendTime;
        this.profileUrl = profileUrl;
        this.concertThumbnailUrl = concertThumbnailUrl;
        this.ticketOpenType = ticketOpenType;
        this.unReadMessageCount = unReadMessageCount;
    }
}
