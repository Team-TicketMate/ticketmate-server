package com.ticketmate.backend.admin.portfolio.application.dto.view;

import com.ticketmate.backend.member.core.constant.MemberType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioAdminInfo(
    UUID portfolioId,
    UUID memberId,
    String nickname,
    String phone,
    String profileImgStoredPath, // 프로필 이미지 저장 경로
    MemberType memberType,
    String portfolioDescription,
    List<String> portfolioImgStoredPathList, // 포트폴리오 이미지 저장 경로 List
    Instant createdDate,
    Instant updatedDate
) {

}
