package com.ticketmate.backend.admin.portfolio.application.mapper;

import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioAdminInfo;

public interface PortfolioAdminMapper {

  /**
   * PortfolioAdminInfo -> PortfolioAdminResponse (DTO -> DTO)
   * 이미지 storedPath -> publicUrl 변환
   */
  PortfolioAdminResponse toPortfolioAdminResponse(PortfolioAdminInfo info);
}
