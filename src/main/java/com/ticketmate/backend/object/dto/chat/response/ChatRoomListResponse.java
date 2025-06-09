package com.ticketmate.backend.object.dto.chat.response;

import com.ticketmate.backend.object.constants.TicketOpenType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomListResponse {
    private String roomId;  // 채팅방 PK
    private String chatRoomName;  // 상대방 닉네임 출력
    private String lastChatMessage;  // 마지막 채팅 메시지
    private LocalDateTime lastChatSendTime;  // 마지막으로 채팅된 시간
    private String profileImg; // 상대방 프로필 사진
    private String concertImg; // 콘서트 썸네일 사진
    private TicketOpenType isPreOpen;  // 선예매/일예 구분
    private int unRead;  // 읽지 않은 메시지

    @Builder
    public ChatRoomListResponse(String roomId, String chatRoomName, String lastChatMessage, LocalDateTime lastChatSendTime, String profileImg, String concertImg, TicketOpenType isPreOpen, int unRead) {
        this.roomId = roomId;
        this.chatRoomName = chatRoomName;
        this.lastChatMessage = lastChatMessage;
        this.lastChatSendTime = lastChatSendTime;
        this.profileImg = profileImg;
        this.concertImg = concertImg;
        this.isPreOpen = isPreOpen;
        this.unRead = unRead;
    }
}
