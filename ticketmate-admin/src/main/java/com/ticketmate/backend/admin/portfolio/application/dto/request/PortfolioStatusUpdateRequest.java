package com.ticketmate.backend.admin.portfolio.application.dto.request;

import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStatusUpdateRequest {

  @NotNull(message = "portfolioStatus가 비어있습니다")
  private PortfolioStatus portfolioStatus;
}
