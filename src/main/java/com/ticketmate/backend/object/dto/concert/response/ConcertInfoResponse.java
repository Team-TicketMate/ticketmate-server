package com.ticketmate.backend.object.dto.concert.response;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import lombok.*;

import java.util.List;

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
    private List<ConcertDateInfoResponse> concertDateInfoResponseList; // 공연일자 List
    private List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponses; // 티켓 오픈일 List
    private TicketReservationSite ticketReservationSite; // 예매처
}
