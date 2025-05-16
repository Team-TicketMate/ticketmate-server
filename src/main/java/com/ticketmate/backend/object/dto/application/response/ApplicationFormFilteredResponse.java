package com.ticketmate.backend.object.dto.application.response;

import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import com.ticketmate.backend.object.constants.TicketOpenType;
import lombok.*;

import java.time.LocalDateTime;
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

    private LocalDateTime performanceDate; // 공연 일자

    private LocalDateTime openDate; // 티켓 오픈일

    private TicketOpenType ticketOpenType; // 선예매/일반예매 타입

    private Integer requestCount; // 매수

    private List<HopeAreaResponse> hopeAreaResponseList; // 희망구역 리스트

    private String requestDetails; // 요청사항

    private ApplicationFormStatus applicationFormStatus; // 신청서 상태
}
