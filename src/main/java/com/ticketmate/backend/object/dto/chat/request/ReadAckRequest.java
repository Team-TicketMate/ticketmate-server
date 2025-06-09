package com.ticketmate.backend.object.dto.chat.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReadAckRequest {
    private String uptoMessageId;  // 마지막으로 본 메시지 ID
    private LocalDateTime readAt;  // 읽은 시각
}
