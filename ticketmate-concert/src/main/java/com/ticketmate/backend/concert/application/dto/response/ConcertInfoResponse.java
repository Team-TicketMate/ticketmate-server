package com.ticketmate.backend.concert.application.dto.response;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
  private List<TicketOpenDateInfoResponse> ticketOpenDateInfoResponseList; // 티켓 오픈일 List
  private TicketReservationSite ticketReservationSite; // 예매처

  public static ConcertInfoResponse of(Concert concert, List<ConcertDate> concertDateList, List<TicketOpenDate> ticketOpenDateList) {
    String concertHallName = concert.getConcertHall().getConcertHallName() != null
        ? concert.getConcertHall().getConcertHallName()
        : null;

    return ConcertInfoResponse.builder()
        .concertName(concert.getConcertName())
        .concertHallName(concertHallName)
        .concertThumbnailUrl(concert.getConcertThumbnailUrl())
        .seatingChartUrl(concert.getSeatingChartUrl())
        .concertType(concert.getConcertType())
        .concertDateInfoResponseList(
            concertDateList.stream()
                .map(ConcertDateInfoResponse::from)
                .collect(Collectors.toList())
        )
        .ticketOpenDateInfoResponseList(
            ticketOpenDateList.stream()
                .map(TicketOpenDateInfoResponse::from)
                .collect(Collectors.toList())
        )
        .ticketReservationSite(concert.getTicketReservationSite())
        .build();
  }
}
