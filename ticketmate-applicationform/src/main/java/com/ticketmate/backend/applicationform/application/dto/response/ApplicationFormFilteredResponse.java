package com.ticketmate.backend.applicationform.application.dto.response;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationFormFilteredResponse(
    UUID applicationFormId, // 신청서 PK
    String concertName, // 공연 제목
    String concertThumbnailUrl, // 공연 썸네일 URL
    String agentNickname, // 대리인 닉네임
    String clientNickname, // 의뢰인 닉네임
    LocalDateTime submittedDate, // 신청 일자
    ApplicationFormStatus applicationFormStatus, // 신청서 상태
    TicketOpenType ticketOpenType // 선예매/일반예매 타입
) {

}
