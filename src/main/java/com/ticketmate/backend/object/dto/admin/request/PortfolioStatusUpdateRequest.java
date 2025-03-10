package com.ticketmate.backend.object.dto.admin.request;

import com.ticketmate.backend.object.constants.PortfolioType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PortfolioStatusUpdateRequest {
    @Schema(defaultValue = "REVIEW_COMPLETED")
    private PortfolioType portfolioType;
}
