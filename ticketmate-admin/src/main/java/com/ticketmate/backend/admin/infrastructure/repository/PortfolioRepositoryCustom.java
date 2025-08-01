package com.ticketmate.backend.admin.infrastructure.repository;

import com.ticketmate.backend.admin.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortfolioRepositoryCustom {

  Page<PortfolioFilteredAdminResponse> filteredPortfolio(
      String username,
      String nickname,
      String name,
      PortfolioType portfolioType,
      Pageable pageable
  );

}
