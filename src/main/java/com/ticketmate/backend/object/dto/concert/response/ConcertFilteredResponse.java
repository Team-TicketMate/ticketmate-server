package com.ticketmate.backend.object.dto.concert.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertFilteredResponse {
    private UUID concertId; // 공연 PK
    private String concertName; // 공연 제목
    private LocalDateTime ticketPreOpenDate; // 티켓 선예매 오픈일
    private LocalDateTime ticketGeneralOpenDate; // 티켓 일반 예매 오픈일
    private LocalDateTime startDate; // 공연 시작일
    private LocalDateTime endDate; // 공연 종료일
    private String concertThumbnailUrl; // 공연 썸네일 URL
}
