package com.ticketmate.backend.object.dto.application.request;

import com.ticketmate.backend.object.constants.TicketOpenType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormDuplicateRequest {
    @NotNull(message = "대리인 PK 값을 입력하세요")
    private UUID agentId;
    @NotNull(message = "공연 PK 값을 입력하세요")
    private UUID concertId;
    @NotNull(message = "선예매/일반예매 타입을 입력하세요")
    private TicketOpenType ticketOpenType;
}
