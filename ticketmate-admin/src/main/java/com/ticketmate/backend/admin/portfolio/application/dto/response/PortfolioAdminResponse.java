package com.ticketmate.backend.admin.portfolio.application.dto.response;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.vo.Phone;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortfolioAdminResponse(
    UUID portfolioId,
    UUID memberId,
    String nickname,
    Phone phone,
    String profileUrl,
    MemberType memberType,
    String portfolioDescription,
    List<String> portfolioImgList,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {

  public PortfolioAdminResponse {
    portfolioImgList = CommonUtil.nullOrEmpty(portfolioImgList) ? List.of() : List.copyOf(portfolioImgList);
  }
}
