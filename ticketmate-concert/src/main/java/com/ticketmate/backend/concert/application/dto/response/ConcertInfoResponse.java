package com.ticketmate.backend.concert.application.dto.response;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.util.List;

public record ConcertInfoResponse(
    String concertName, // 공연 제목
    String concertHallName, // 공연장 이름
    String concertThumbnailUrl, // 공연 썸네일 이미지 URL
    String seatingChartUrl, // 좌석 배치도 URL
    ConcertType concertType, // 공연 카테고리
    List<ConcertDateInfoResponse> concertDateInfoResponseList, // 공연일자 List
    List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList, // 티켓 오픈일 List
    TicketReservationSite ticketReservationSite // 예매처
) {

}
