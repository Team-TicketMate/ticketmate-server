package com.ticketmate.backend.object.dto.concert.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConcertDateRequest {

    @NotNull(message = "공연일자를 입력해주세요")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-11T20:00")
    private LocalDateTime concertDate;

    @Min(value = 1, message = "최대 예매 매수는 1 이상이여야 합니다")
    @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
    @NotNull(message = "공연 회차를 입력해주세요")
    @Schema(defaultValue = "1")
    private Integer session;
}
