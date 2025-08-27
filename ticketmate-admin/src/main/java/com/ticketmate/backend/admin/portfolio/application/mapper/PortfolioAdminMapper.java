package com.ticketmate.backend.admin.portfolio.application.mapper;

import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.admin.portfolio.application.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.applicationform.application.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PortfolioAdminMapper {

  // Portfolio -> PortfolioFilteredAdminResponse (엔티티 -> DTO)
  @Mapping(source = "member.memberId", target = "memberId")
  @Mapping(source = "member.username", target = "username")
  @Mapping(source = "member.nickname", target = "nickname")
  @Mapping(source = "member.name", target = "name")
  PortfolioFilteredAdminResponse toPortfolioFilteredAdminResponse(Portfolio portfolio);

  // Portfolio -> PortfolioForAdminResponse (엔티티 -> DTO)
  @Mapping(source = "member.memberId", target = "memberId")
  @Mapping(source = "member.nickname", target = "nickname")
  @Mapping(source = "member.phone", target = "phone")
  @Mapping(source = "member.profileUrl", target = "profileUrl")
  @Mapping(source = "member.memberType", target = "memberType")
  @Mapping(target = "portfolioImgList", expression = "java(mapToFilePathList(portfolio.getPortfolioImgList()))")
  PortfolioForAdminResponse toPortfolioForAdminResponse(Portfolio portfolio);

  // PortfolioImg 엔티티 리스트에서 각 filePath만 추출하여 String 리스트로 변환
  @Named("mapToFilePathList")
  default List<String> mapToFilePathList(List<PortfolioImg> imgList) {
    if (CommonUtil.nullOrEmpty(imgList)) {
      return List.of();
    }
    return imgList.stream()
        .map(PortfolioImg::getFilePath)
        .collect(Collectors.toList());
  }

  // RejectionReason -> RejectionReasonResponse (엔티티 -> DTO)
  RejectionReasonResponse toRejectionReasonResponse(RejectionReason rejectionReason);
}
