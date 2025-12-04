package com.ticketmate.backend.admin.portfolio.application.mapper;

import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioAdminInfo;
import com.ticketmate.backend.admin.portfolio.application.dto.view.PortfolioFilteredAdminInfo;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortfolioAdminMapperImpl implements PortfolioAdminMapper {

  private final StorageService storageService;

  @Override
  public PortfolioAdminResponse toPortfolioAdminResponse(PortfolioAdminInfo info) {
    List<String> portfolioImgPublicUrlList = new ArrayList<>();
    if (!CommonUtil.nullOrEmpty(info.portfolioImgStoredPathList())) {
      portfolioImgPublicUrlList = info.portfolioImgStoredPathList().stream()
        .map(storageService::generatePublicUrl)
        .collect(Collectors.toList());
    }

    return new PortfolioAdminResponse(
      info.portfolioId(),
      info.memberId(),
      info.nickname(),
      info.phone(),
      storageService.generatePublicUrl(info.profileImgStoredPath()),
      info.memberType(),
      info.portfolioDescription(),
      portfolioImgPublicUrlList,
      TimeUtil.toLocalDateTime(info.createdDate()),
      TimeUtil.toLocalDateTime(info.updatedDate())
    );
  }

  @Override
  public PortfolioFilteredAdminResponse toPortfolioFilteredAdminResponse(PortfolioFilteredAdminInfo info) {
    return new PortfolioFilteredAdminResponse(
      info.portfolioId(),
      info.memberId(),
      info.username(),
      info.nickname(),
      info.name(),
      info.portfolioStatus(),
      TimeUtil.toLocalDateTime(info.createdDate()),
      TimeUtil.toLocalDateTime(info.updatedDate())
    );
  }
}
