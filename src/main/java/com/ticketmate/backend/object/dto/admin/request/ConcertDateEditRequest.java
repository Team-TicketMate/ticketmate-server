package com.ticketmate.backend.object.dto.admin.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertDateEditRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(defaultValue = "2025-02-11T20:00")
    private LocalDateTime performanceDate; // 공연 일자

    @Min(value = 1, message = "최대 예매 매수는 1 이상이어야 합니다")
    @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
    @Schema(defaultValue = "1")
    private Integer session; // 회차
}
