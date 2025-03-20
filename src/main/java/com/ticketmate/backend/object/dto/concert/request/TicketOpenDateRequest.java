package com.ticketmate.backend.object.dto.concert.request;

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
public class TicketOpenDateRequest {

    private LocalDateTime ticketOpenDate; // 티켓 오픈일

    @Min(value = 1, message = "최대 예매 매수는 1 이상이여야 합니다")
    @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
    private Integer requestMaxCount; // 최대 예매 매수

    private Boolean isBankTransfer; // 무통장 입금 여부

    @NotNull(message = "선예매 여부를 선택하세요")
    @Schema(defaultValue = "false")
    private Boolean isPreOpen; // 선예매, 일반예매 여부
}
