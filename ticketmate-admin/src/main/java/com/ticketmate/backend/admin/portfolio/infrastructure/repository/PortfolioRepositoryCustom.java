package com.ticketmate.backend.admin.portfolio.infrastructure.repository;

import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioAdminInfo;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioFilteredAdminInfo;
import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortfolioRepositoryCustom {

  /**
   * 포트폴리오 필터링 조회
   */
  Page<PortfolioFilteredAdminInfo> filteredPortfolio(
      String username,
      String nickname,
      String name,
      PortfolioStatus portfolioStatus,
      Pageable pageable
  );

  /**
   * 포트폴리오 상세 조회 (fetchJoin)
   */
  PortfolioAdminInfo findPortfolioAdminInfoByPortfolioId(UUID portfolioId);

}
