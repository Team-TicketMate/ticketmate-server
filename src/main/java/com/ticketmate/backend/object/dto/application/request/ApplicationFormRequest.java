package com.ticketmate.backend.object.dto.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

    @NotNull(message = "티켓 요청 매수를 입력해주세요")
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    @Schema(defaultValue = "1")
    private Integer requestCount; // 요청매수

    private List<HopeAreaRequest> hopeAreaList = new ArrayList<>();

    private String requestDetails; // 요청사항
}
