package com.ticketmate.backend.concertagentavailability.application.dto.response;

import com.ticketmate.backend.concert.core.constant.RecruitmentStatus;

import java.util.UUID;

public record AgentConcertSettingResponse(
    UUID concertId, // 공연 PK
    String concertName, // 공연 제목
    String concertThumbnailUrl, // 공연 썸네일 URL
    RecruitmentStatus status, // 모집 여부
    int matchedClientCount, // 매칭된 의뢰인 수
    boolean accepting // on/off
) {
}
