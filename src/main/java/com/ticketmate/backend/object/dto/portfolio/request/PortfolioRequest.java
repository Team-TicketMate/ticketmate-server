package com.ticketmate.backend.object.dto.portfolio.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PortfolioRequest {

  @NotBlank(message = "자기소개를 입력하세요")
  @Size(min = 20, max = 200, message = "자기소개는 20자 이상, 200자 이하로 입력해 주세요.")
  @Schema(defaultValue = "NCT드림, 세븐틴, 투바투, 보넥도등 남자 아이돌 티켓팅 전문입니다. 최대한 고객님의 니즈에 맞춰 자리 잡아드립니다.")
  private String portfolioDescription;

  @Size(min = 1, max = 20, message = "포트폴리오 첨부파일은 최소 1개, 최대 20개까지 등록 가능합니다.")
  private List<MultipartFile> portfolioImgList;
}
