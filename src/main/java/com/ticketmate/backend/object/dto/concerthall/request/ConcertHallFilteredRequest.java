package com.ticketmate.backend.object.dto.concerthall.request;

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
public class ConcertHallFilteredRequest {

    // 기본값 할당 (1페이지 30개, 최신순)
    public ConcertHallFilteredRequest() {
        this.pageNumber = 0;
        this.pageSize = 30;
        this.sortField = "created_date";
        this.sortDirection = "DESC";
    }

    @Schema(defaultValue = "장충")
    private String concertHallName; // 공연장 명 (검색어)

    @Schema(defaultValue = "11")
    private Integer cityCode; // 지역 코드

    @Schema(defaultValue = "0")
    @Min(value = 0, message = "페이지 번호 인덱스에 음수는 입력될 수 없습니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageNumber; // 페이지 번호

    @Schema(defaultValue = "30")
    @Min(value = 0, message = "페이지 사이즈에 음수는 입력될 수 없습니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageSize; // 페이지 사이즈

    @Schema(defaultValue = "created_date")
    @Pattern(regexp = "^(created_date)$")
    private String sortField; // 정렬 조건 (생성일, 선 예매 오픈일, 티켓 오픈일, 공연시간)

    @Schema(defaultValue = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$", message = "sortDirection에는 'ASC', 'DESC' 만 입력 가능합니다.")
    private String sortDirection; // ASC, DESC
}
