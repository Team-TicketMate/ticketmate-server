package com.ticketmate.backend.domain.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class PortfolioFilteredAdminResponse {

  private UUID portfolioId;

  private UUID memberId;

  private String username;

  private String nickname;

  private String name;

  private PortfolioType portfolioType;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime createdDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime updatedDate;
}
