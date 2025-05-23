package com.ticketmate.backend.object.dto.application.response;

import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import com.ticketmate.backend.object.constants.TicketOpenType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormFilteredResponse {

    private UUID applicationFormId; // 신청서 PK

    private UUID clientId; // 의뢰인 PK

    private UUID agentId; // 대리인 PK

    private UUID concertId; // 콘서트 PK

    private LocalDateTime openDate; // 티켓 예매일

    @Builder.Default
    private List<ApplicationFormDetailResponse> applicationFormDetailResponseList = new ArrayList<>(); // 신청서 세부사항 리스트

    private ApplicationFormStatus applicationFormStatus; // 신청서 상태

    private TicketOpenType ticketOpenType; // 선예매/일반예매 타입
}
