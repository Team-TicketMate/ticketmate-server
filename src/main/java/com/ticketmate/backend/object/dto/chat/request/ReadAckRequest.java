package com.ticketmate.backend.object.dto.chat.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReadAckRequest {

  private String lastReadMessageId;  // 마지막으로 본 메시지 ID
  private LocalDateTime readDate;  // 읽은 시각
}
