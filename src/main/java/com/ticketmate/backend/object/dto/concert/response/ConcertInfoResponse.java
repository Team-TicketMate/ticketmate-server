package com.ticketmate.backend.object.dto.concert.response;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConcertInfoResponse {
    private String concertName; // 공연명
    private String concertHallName; // 공연장 이름
    private String concertThumbnailUrl; // 공연 썸네일 이미지 url
    private String seatingChartUrl; // 좌석 배치도 url
    private ConcertType concertType; // 공연 카테고리
    private LocalDateTime startDate; // 공연 시작 일자
    private LocalDateTime endDate; // 공연 종료 일자
    private LocalDateTime preOpenDate; // 선예매 오픈일
    private Integer preOpenRequestMaxCount; // 선예매 최대 예매 매수
    private Boolean preOpenIsBankTransfer; // 선예매 무통장 입금 가능 여부
    private LocalDateTime generalOpenDate; // 일반 예매 오픈일
    private Integer generalOpenRequestMaxCount; // 일반 예매 최대 예매 매수
    private Boolean generalOpenIsBankTransfer; // 일반 예매 무통장 입금 가능 여부
    private TicketReservationSite ticketReservationSite; // 예매처
}
