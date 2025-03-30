package com.ticketmate.backend.object.dto.concert.request;

import com.ticketmate.backend.object.constants.ConcertType;
import com.ticketmate.backend.object.constants.TicketReservationSite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

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
    private String concertName; // 공연 제목

    @Schema(defaultValue = "인스파이어 아레나")
    private String concertHallName; // 공연장

    @Schema(defaultValue = "CONCERT")
    private ConcertType ConcertType; // 공연 카테고리

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
    @Pattern(regexp = "^(created_date|ticket_open_date)$")
    private String sortField; // 정렬 조건

    @Schema(defaultValue = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$")
    private String sortDirection;
}
