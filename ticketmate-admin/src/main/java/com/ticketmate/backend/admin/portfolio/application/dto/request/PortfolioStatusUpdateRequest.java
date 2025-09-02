package com.ticketmate.backend.admin.portfolio.application.dto.request;

import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PortfolioStatusUpdateRequest {

  @NotNull(message = "변경하려는 포트폴리오 상태를 입력하세요.")
  private PortfolioStatus portfolioStatus;
}
