package com.ticketmate.backend.object.dto.concert.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.TicketOpenType;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TicketOpenDateInfoResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime openDate; // 티켓 오픈일
    private Integer requestMaxCount; // 최대 예매 매수
    private Boolean isBankTransfer; // 무통장 입금 여부
    private TicketOpenType ticketOpenType; // 선예매, 일반예매 여부
}
