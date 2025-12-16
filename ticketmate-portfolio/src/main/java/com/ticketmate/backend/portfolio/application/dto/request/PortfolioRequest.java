package com.ticketmate.backend.portfolio.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequest {

  @NotBlank(message = "portfolioDescription이 비어있습니다")
  @Size(min = 20, max = 200, message = "portfolioDescription은 최소 20자 최대 200자 입력 가능합니다")
  private String portfolioDescription;

  @Size(min = 1, max = 20, message = "portfolioImgList는 최소 1개 최대 20개 등록 가능합니다.")
  private List<MultipartFile> portfolioImgList;
}
