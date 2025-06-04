package com.ticketmate.backend.object.dto.chat.response;

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
    private Integer messageCount;  // 현재 쌓인 메시지 (보지않은..)
    private String profileImg; // 상대방 프로필 사진
    private String concertImg; // 콘서트 썸네일 사진
    private Boolean isPreOpen;  // 선예매/일예 구분

    @Builder
    public ChatRoomListResponse(String roomId, String chatRoomName, String lastChatMessage, LocalDateTime lastChatSendTime, Integer messageCount, String profileImg, String concertImg, Boolean isPreOpen) {
        this.roomId = roomId;
        this.chatRoomName = chatRoomName;
        this.lastChatMessage = lastChatMessage;
        this.lastChatSendTime = lastChatSendTime;
        this.messageCount = messageCount;
        this.profileImg = profileImg;
        this.concertImg = concertImg;
        this.isPreOpen = isPreOpen;
    }
}
