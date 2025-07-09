package com.ticketmate.backend.domain.admin.dto.request;

import com.ticketmate.backend.domain.concert.domain.constant.ConcertType;
import com.ticketmate.backend.domain.concert.domain.constant.TicketReservationSite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ConcertInfoRequest {

  @NotBlank(message = "공연 제목을 입력하세요.")
  @Schema(defaultValue = "[Play＆Stay]TOMORROW X TOGETHER WORLD TOUR 〈ACT : PROMISE〉 - EP. 2 - IN INCHEON + Hotels")
  private String concertName; // 공연 제목

  private UUID concertHallId; // 공연장 PK

  @NotNull(message = "공연 카테고리를 입력하세요.")
  @Schema(defaultValue = "CONCERT")
  private ConcertType concertType; // 공연 카테고리

  @NotNull(message = "콘서트 썸네일 이미지를 업로드하세요.")
  private MultipartFile concertThumbNail; // 공연 썸네일

  private MultipartFile seatingChart; // 좌석 배치도

  @Schema(defaultValue = "INTERPARK_TICKET")
  private TicketReservationSite ticketReservationSite; // 예매 사이트

  @Valid
  @NotEmpty(message = "공연 날짜를 입력하세요.")
  private List<ConcertDateRequest> concertDateRequestList; // 공연 날짜 DTO

  @Valid
  @NotEmpty(message = "티켓 오픈일 정보를 입력하세요")
  private List<TicketOpenDateRequest> ticketOpenDateRequestList; // 티켓 오픈일 DTO
}
