package com.ticketmate.backend.admin.application.dto.response;

import com.ticketmate.backend.member.core.constant.MemberType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PortfolioForAdminResponse {

  private UUID portfolioId;

  private UUID memberId;

  private String nickname;

  private String phone;

  private String profileUrl;

  private MemberType memberType;

  private String portfolioDescription;

  private List<String> portfolioImgList;

  private LocalDateTime createdDate;

  private LocalDateTime updatedDate;
}
