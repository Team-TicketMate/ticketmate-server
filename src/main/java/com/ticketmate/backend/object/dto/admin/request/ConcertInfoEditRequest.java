package com.ticketmate.backend.object.dto.admin.request;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ConcertInfoEditRequest {

    @Schema(defaultValue = "[Play＆Stay]TOMORROW X TOGETHER WORLD TOUR 〈ACT : PROMISE〉 - EP. 2 - IN INCHEON + Hotels")
    private String concertName; // 공연명

    private UUID concertHallId; // 공연장 PK

    @Schema(defaultValue = "CONCERT")
    private ConcertType concertType; // 공연 카테고리

    private MultipartFile concertThumbNail; // 공연 썸네일

    private MultipartFile seatingChart; // 좌석 배치도

    @Schema(defaultValue = "INTERPARK_TICKET")
    private TicketReservationSite ticketReservationSite; // 예매 사이트

    private List<ConcertDateRequest> concertDateRequestList; // 공연 날짜 DTO

    private List<TicketOpenDateRequest> ticketOpenDateRequestList; // 티켓 오픈일 DTO
}
