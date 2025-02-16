package com.ticketmate.backend.object.dto.portfolio.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PortfolioRequest {
    @NotBlank(message = "자기소개를 입력하세요")
    @Size(min = 20, max = 200, message = "자기소개는 20자 이상, 200자 이하로 입력해 주세요.")
    @Schema(defaultValue = "NCT드림, 세븐틴, 투바투, 보넥도등 남자 아이돌 티켓팅 전문입니다. 최대한 고객님의 니즈에 맞춰 자리 잡아드립니다.")
    private String publicRelations;

    private List<MultipartFile> portfolioImg;
}
