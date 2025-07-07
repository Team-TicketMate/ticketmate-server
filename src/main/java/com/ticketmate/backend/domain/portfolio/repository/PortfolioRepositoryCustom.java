package com.ticketmate.backend.domain.portfolio.repository;

import com.ticketmate.backend.domain.admin.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
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
