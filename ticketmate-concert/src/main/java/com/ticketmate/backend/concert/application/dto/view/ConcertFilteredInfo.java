package com.ticketmate.backend.concert.application.dto.view;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConcertFilteredInfo(
    UUID concertId, // 공연 PK
    String concertName, // 공연 제목
    String concertHallName, // 공연장 이름
    ConcertType concertType, // 공연 카테고리
    TicketReservationSite ticketReservationSite, // 예매처
    LocalDateTime ticketPreOpenDate, // 티켓 선예매 오픈일
    Boolean preOpenBankTransfer, // 선예매 무통장 여부
    LocalDateTime ticketGeneralOpenDate, // 티켓 일반 예매 오픈일
    Boolean generalOpenBankTransfer, // 일반 예매 무통장 여부
    LocalDateTime startDate, // 공연 시작일
    LocalDateTime endDate, // 공연 종료일
    String concertThumbnailStoredPath, // 공연 썸네일 저장 경로
    String seatingChartStoredPath // 좌석배치도 저장 경로
) {

}
