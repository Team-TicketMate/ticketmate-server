package com.ticketmate.backend.admin.portfolio.application.dto.response;

import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortfolioFilteredAdminResponse(
    UUID portfolioId,
    UUID memberId,
    String username,
    String nickname,
    String name,
    PortfolioStatus portfolioStatus,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {

}
