package com.ticketmate.backend.concert.application.dto.response;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import java.time.LocalDateTime;
import java.util.UUID;
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
public class ConcertFilteredResponse {

  private UUID concertId; // 공연 PK

  private String concertName; // 공연 제목

  private String concertHallName; // 공연장 이름

  private ConcertType concertType; // 공연 카테고리

  private TicketReservationSite ticketReservationSite; // 예매처

  private LocalDateTime ticketPreOpenDate; // 티켓 선예매 오픈일

  private Boolean preOpenBankTransfer; // 선예매 무통장 여부

  private LocalDateTime ticketGeneralOpenDate; // 티켓 일반 예매 오픈일

  private Boolean generalOpenBankTransfer; // 일반 예매 무통장 여부

  private LocalDateTime startDate; // 공연 시작일

  private LocalDateTime endDate; // 공연 종료일

  private String concertThumbnailUrl; // 공연 썸네일 URL

  private String seatingChartUrl; // 좌석배치도 URL
}
