package com.ticketmate.backend.portfolio.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
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

  @NotBlank
  @NotBlankErrorCode(ErrorCode.PORTFOLIO_DESCRIPTION_EMPTY)
  @Size(min = 20, max = 200)
  @SizeErrorCode(ErrorCode.PORTFOLIO_DESCRIPTION_LENGTH_INVALID)
  private String portfolioDescription;

  @Size(min = 1, max = 20)
  @SizeErrorCode(ErrorCode.PORTFOLIO_IMG_LIST_SIZE_INVALID)
  private List<MultipartFile> portfolioImgList;
}
