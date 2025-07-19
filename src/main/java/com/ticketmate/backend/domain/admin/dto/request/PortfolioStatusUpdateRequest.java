package com.ticketmate.backend.domain.admin.dto.request;

import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import io.swagger.v3.oas.annotations.media.Schema;
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

  @Schema(defaultValue = "APPROVED")
  @Pattern(regexp = "^(APPROVED|REJECTED)$")
  private PortfolioType portfolioType;
}
