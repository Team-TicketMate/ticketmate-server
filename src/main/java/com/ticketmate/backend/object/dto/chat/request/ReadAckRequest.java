package com.ticketmate.backend.object.dto.chat.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReadAckRequest {
    private String lastReadMessageId;  // 마지막으로 본 메시지 ID
    private LocalDateTime readDate;  // 읽은 시각
}
