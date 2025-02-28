package com.ticketmate.backend.object.dto.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HopeAreaRequest {

    @NotNull(message = "우선순위를 입력하세요. 1 ~ 10 정수")
    @Min(value = 1, message = "순위는 1부터 10 사이의 정수만 입력 가능합니다.")
    @Max(value = 10, message = "순위는 1부터 10 사이의 정수만 입력 가능합니다.")
    @Schema(defaultValue = "1")
    private Integer priority; // 순위

    @NotNull(message = "희망 구역을 입력하세요.")
    @Schema(defaultValue = "A구역")
    private String location; // 구역

    @NotNull(message = "희망 가격을 입력하세요.(원 단위)")
    @Min(value = 0, message = "희망 가격은 0원 이상이여야 합니다.")
    @Schema(defaultValue = "150000")
    private Long price; // 가격
}
