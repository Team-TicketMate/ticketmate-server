package com.ticketmate.backend.object.dto.application.request;

import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationFormFilteredRequest {

    // 기본값 할당 (1페이지 30개, 최신순)
    public ApplicationFormFilteredRequest() {
        this.pageNumber = 0;
        this.pageSize = 30;
        this.sortField = "created_date";
        this.sortDirection = "DESC";
    }

    private UUID clientId; // 의뢰인 PK

    private UUID agentId; // 대리인 PK

    private UUID concertId; // 콘서트 PK

    @Schema(defaultValue = "1")
    private Integer requestCount; // 매수

    @Schema(defaultValue = "PENDING")
    private ApplicationFormStatus applicationFormStatus; // 신청서 상태

    @Schema(defaultValue = "0")
    @Min(value = 0, message = "페이지 번호 인덱스에 음수는 입력될 수 없습니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageNumber; // 페이지 번호

    @Schema(defaultValue = "30")
    @Min(value = 0, message = "페이지 사이즈에 음수는 입력될 수 없습니다.")
    @Max(value = Integer.MAX_VALUE, message = "정수 최대 범위를 넘을 수 없습니다.")
    private Integer pageSize; // 페이지 사이즈

    @Schema(defaultValue = "created_date")
    @Pattern(regexp = "^(created_date|request_count)$")
    private String sortField; // 정렬 조건 (생성일, 매수)

    @Schema(defaultValue = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$", message = "sortDirection에는 'ASC', 'DESC' 만 입력 가능합니다.")
    private String sortDirection; // ASC, DESC
}
