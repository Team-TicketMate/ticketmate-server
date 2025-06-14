package com.ticketmate.backend.object.dto.chat.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReadAckRequest {
    private String lastReadMessageId;  // 마지막으로 본 메시지 ID
    private LocalDateTime readDate;  // 읽은 시각
}
