package com.ticketmate.backend.admin.application.dto.request;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertInfoEditRequest {

  private String concertName; // 공연명

  private UUID concertHallId; // 공연장 PK

  private ConcertType concertType; // 공연 카테고리

  private MultipartFile concertThumbNail; // 공연 썸네일

  private MultipartFile seatingChart; // 좌석 배치도

  private TicketReservationSite ticketReservationSite; // 예매 사이트

  @Valid
  private List<ConcertDateRequest> concertDateRequestList; // 공연 날짜 DTO

  @Valid
  private List<TicketOpenDateRequest> ticketOpenDateRequestList; // 티켓 오픈일 DTO
}
