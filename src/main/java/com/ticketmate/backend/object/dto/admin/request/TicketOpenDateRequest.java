package com.ticketmate.backend.object.dto.admin.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.TicketOpenType;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime openDate; // 티켓 오픈일

    @Min(value = 1, message = "최대 예매 매수는 1 이상이여야 합니다")
    @Max(value = Integer.MAX_VALUE, message = "최대 예매 매수는 정수 최대 범위를 넘을 수 없습니다.")
    private Integer requestMaxCount; // 최대 예매 매수

    private Boolean isBankTransfer; // 무통장 입금 여부

    @NotNull(message = "선예매/일반예매 타입을 입력하세요")
    private TicketOpenType ticketOpenType; // 선예매, 일반예매 타입
}
