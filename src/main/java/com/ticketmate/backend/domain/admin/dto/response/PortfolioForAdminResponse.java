package com.ticketmate.backend.domain.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
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
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime createdDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime updatedDate;
}
