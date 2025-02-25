package com.ticketmate.backend.object.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PortfolioSearchRequest {
    @Builder.Default
    @Schema(defaultValue = "1")
    private int index = 1; // 1페이지부터 시작

    public long getOffset() {
        // page가 1보다 작으면 1로 보정
        int currentPage = Math.max(1, index);
        // size는 고정 10
        return (long) (currentPage - 1) * 10;
    }
}
