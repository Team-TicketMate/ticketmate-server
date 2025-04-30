package com.ticketmate.backend.object.dto.admin.request;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
    private MultipartFile concertThumbNail; // 콘서트 썸네일

    private MultipartFile seatingChart; // 좌석 배치도

    @Schema(defaultValue = "INTERPARK_TICKET")
    private TicketReservationSite ticketReservationSite; // 예매 사이트

    private List<ConcertDateRequest> concertDateRequestList; // 공연 날짜 DTO

    private List<TicketOpenDateRequest> ticketOpenDateRequestList; // 티켓 오픈일 DTO
}
