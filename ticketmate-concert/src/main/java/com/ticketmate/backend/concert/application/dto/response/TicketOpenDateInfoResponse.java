package com.ticketmate.backend.concert.application.dto.response;

import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.time.LocalDateTime;

public record TicketOpenDateInfoResponse(
    LocalDateTime openDate, // 티켓 오픈일
    Integer requestMaxCount, // 최대 예매 매수
    Boolean isBankTransfer, // 무통장 입금 여부
    TicketOpenType ticketOpenType // 선예매, 일반예매 여부
) {

}
