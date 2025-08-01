package com.ticketmate.backend.chat.application.dto.request;

import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoomRequest {

  @NotBlank(message = "대리인 PK를 입력하세요")
  private UUID agentId;

  @NotBlank(message = "의뢰인 PK를 입력하세요")
  private UUID clientId;

  @NotBlank(message = "공연 PK를 입력하세요")
  private UUID concertId;

  @NotBlank(message = "공연 선예매/일반예매 타입을 입력하세요")
  private TicketOpenType ticketOpenType;
}
