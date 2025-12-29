package com.ticketmate.backend.admin.concert.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotEmptyErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.concert.core.constant.ConcertType;
import com.ticketmate.backend.concert.core.constant.TicketReservationSite;
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
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertInfoRequest {

  @NotBlank
  @NotBlankErrorCode(ErrorCode.CONCERT_NAME_EMPTY)
  private String concertName; // 공연 제목

  private UUID concertHallId; // 공연장 PK

  @NotNull
  @NotNullErrorCode(ErrorCode.CONCERT_TYPE_EMPTY)
  private ConcertType concertType; // 공연 카테고리

  @NotNull
  @NotNullErrorCode(ErrorCode.CONCERT_THUMBNAIL_EMPTY)
  private MultipartFile concertThumbNail; // 공연 썸네일

  private MultipartFile seatingChart; // 좌석 배치도

  private TicketReservationSite ticketReservationSite; // 예매 사이트

  @Valid
  @NotEmpty
  @NotEmptyErrorCode(ErrorCode.CONCERT_DATE_REQUEST_LIST_EMPTY)
  private List<ConcertDateRequest> concertDateRequestList; // 공연 날짜 DTO

  @Valid
  @NotEmpty
  @NotEmptyErrorCode(ErrorCode.TICKET_OPEN_DATE_REQUEST_LIST_EMPTY)
  private List<TicketOpenDateRequest> ticketOpenDateRequestList; // 티켓 오픈일 DTO
}
