package com.ticketmate.backend.admin.portfolio.application.mapper;

import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioAdminInfo;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioFilteredAdminInfo;

public interface PortfolioAdminMapper {

  /**
   * PortfolioAdminInfo -> PortfolioAdminResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  PortfolioAdminResponse toPortfolioAdminResponse(PortfolioAdminInfo info);

  /**
   * PortfolioFilteredAdminInfo -> PortfolioFilteredAdminResponse (DTO -> DTO)
   * Instant -> LocalDateTime 변환
   */
  PortfolioFilteredAdminResponse toPortfolioFilteredAdminResponse(PortfolioFilteredAdminInfo info);
}
