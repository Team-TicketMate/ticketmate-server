package com.ticketmate.backend.admin.portfolio.application.dto.view;

import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import java.time.Instant;
import java.util.UUID;

public record PortfolioFilteredAdminInfo(
  UUID portfolioId,
  UUID memberId,
  String username,
  String nickname,
  String name,
  PortfolioStatus portfolioStatus,
  Instant createdDate,
  Instant updatedDate
) {

}
