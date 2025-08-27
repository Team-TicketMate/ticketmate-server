package com.ticketmate.backend.admin.portfolio.application.dto.request;

import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PortfolioStatusUpdateRequest {

  @Pattern(regexp = "^(APPROVED|REJECTED)$")
  private PortfolioType portfolioType;
}
