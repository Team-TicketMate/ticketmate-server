package com.ticketmate.backend.object.dto.concert.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertFilteredRequest {

    // 기본값 할당 (1페이지 30개, 최신순)
    public ConcertFilteredRequest() {
        this.pageNumber = 0;
        this.pageSize = 30;
        this.sortField = "created_date";
        this.sortDirection = "DESC";
    }

    @Schema(defaultValue = "tomorrow")
    private String concertName; // 콘서트 제목

    @Schema(defaultValue = "인스파이어 아레나")
    private String concertHallName; // 공연장

    @Schema(defaultValue = "CONCERT")
    private ConcertType ConcertType; // 공연 카테고리

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-10T00:00:00")
    private LocalDateTime ticketPreOpenStartDate; // 선예매 오픈일 범위 시작일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-13T00:00:00")
    private LocalDateTime ticketPreOpenEndDate; // 선예매 오픈일 범위 종료일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-12T00:00:00")
    private LocalDateTime ticketOpenStartDate; // 티켓 오픈일 범위 시작일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-14T00:00:00")
    private LocalDateTime ticketOpenEndDate; // 티켓 오픈일 범위 종료일

    @Min(value = 1, message = "회차 최솟값은 1입니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 밤위를 넘을 수 없습니다.")
    @Schema(defaultValue = "1")
    private Integer session; // 회차

    @Schema(defaultValue = "INTERPARK_TICKET")
    private TicketReservationSite ticketReservationSite; // 예매처

    @Schema(defaultValue = "0")
    @Min(value = 0, message = "페이지 번호 인덱스에 음수는 입력될 수 없습니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageNumber; // 페이지 번호

    @Schema(defaultValue = "30")
    @Min(value = 0, message = "페이지 사이즈에 음수는 입력될 수 없습니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageSize; // 페이지 사이즈

    @Schema(defaultValue = "created_date")
    @Pattern(regexp = "^(created_date|ticket_pre_open_date|ticket_open_date|duration)$")
    private String sortField; // 정렬 조건 (생성일, 선 예매 오픈일, 티켓 오픈일, 공연시간)

    @Schema(defaultValue = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$", message = "sortDirection에는 'ASC', 'DESC' 만 입력 가능합니다.")
    private String sortDirection; // ASC, DESC
}
