package com.ticketmate.backend.chat.application.dto.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadAckRequest {

  private String lastReadMessageId;  // 마지막으로 본 메시지 ID

  private LocalDateTime readDate;  // 읽은 시각
}
