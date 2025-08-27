package com.ticketmate.backend.admin.portfolio.application.dto.response;

import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class PortfolioFilteredAdminResponse {

  private UUID portfolioId;

  private UUID memberId;

  private String username;

  private String nickname;

  private String name;

  private PortfolioType portfolioType;

  private LocalDateTime createdDate;

  private LocalDateTime updatedDate;
}
