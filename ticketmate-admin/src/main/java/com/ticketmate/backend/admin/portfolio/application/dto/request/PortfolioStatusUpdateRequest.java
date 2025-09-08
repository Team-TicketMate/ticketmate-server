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

  @NotNull(message = "변경하려는 포트폴리오 상태를 입력하세요.")
  private PortfolioStatus portfolioStatus;
}
