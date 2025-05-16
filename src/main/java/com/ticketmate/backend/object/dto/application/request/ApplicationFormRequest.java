package com.ticketmate.backend.object.dto.application.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.TicketOpenType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormRequest {

    @NotNull(message = "대리인 PK 값을 입력해주세요")
    private UUID agentId; // 대리인 PK

    @NotNull(message = "콘서트 PK 값을 입력해주세요")
    private UUID concertId; // 콘서트 PK

    @NotNull(message = "공연일자를 입력해주세요")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(defaultValue = "2025-02-11T20:00:00")
    private LocalDateTime performanceDate; // 공연일자

    @NotNull(message = "티켓 요청 매수를 입력해주세요")
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    @Schema(defaultValue = "1")
    private Integer requestCount; // 요청매수

    private List<HopeAreaRequest> hopeAreaList = new ArrayList<>();

    @Schema(defaultValue = "꼭 잡아주세요...!!")
    private String requestDetails; // 요청사항

    @NotNull(message = "선예매/일반예매 타입을 입력해주세요")
    private TicketOpenType ticketOpenType; // 선예매 여부
}
