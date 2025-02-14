package com.ticketmate.backend.object.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import com.ticketmate.backend.object.postgres.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertInfoRequest {

    private Member member;

    @NotBlank(message = "공연 제목을 입력하세요.")
    @Schema(defaultValue = "[Play＆Stay]TOMORROW X TOGETHER WORLD TOUR 〈ACT : PROMISE〉 - EP. 2 - IN INCHEON + Hotels")
    private String concertName; // 공연 제목

    @NotBlank(message = "공연장 정보를 입력하세요.")
    @Schema(defaultValue = "인스파이어 아레나")
    private String concertHallName; // 공연장 이름

    @NotNull(message = "공연 카테고리를 입력하세요.")
    @Schema(defaultValue = "CONCERT")
    private ConcertType concertType; // 공연 카테고리

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-11T20:00")
    private LocalDateTime ticketPreOpenDate; // 선예매 오픈일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotBlank(message = "티켓 오픈일을 입력하세요")
    @Schema(defaultValue = "2025-02-13T20:00")
    private LocalDateTime ticketOpenDate; // 티켓 오픈일

    @NotNull(message = "공연 시간을 입력하세요.(단위: 분)")
    @Min(value = 1, message = "공연시간은 1분 이상입니다.")
    @Schema(defaultValue = "120")
    private Integer duration; // 공연 시간 (분 단위)

    @NotNull(message = "공연 회차를 입력해주세요. 단일 공연인 경우 1 입력")
    @Schema(defaultValue = "1")
    private Integer session; // 공연 회차

    @NotNull(message = "콘서트 썸네일 이미지를 업로드하세요.")
    private MultipartFile concertThumbNail; // 콘서트 썸네일

    @NotNull(message = "예매 사이트를 입력해주세요.")
    @Schema(defaultValue = "INTERPARK_TICKET")
    private TicketReservationSite ticketReservationSite; // 예매 사이트
}
