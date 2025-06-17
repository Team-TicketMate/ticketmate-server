package com.ticketmate.backend.domain.portfolio.controller;

import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.portfolio.domain.dto.request.PortfolioRequest;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface PortfolioControllerDocs {

  @Operation(
      summary = "의뢰자 -> 대리자로 변경하기 위한 포트폴리오 업로드",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **publicRelations** (String): 자기소개 [필수]
          - **portfolioImg** (MultipartFile): 포트폴리오 이미지 [필수]
          
          
          ### 유의사항
          - 이미지가 공백 혹은 첨부가 안되어있으면 에러가 발생합니다.
          - 포트폴리오 검증을 받기위해 이미지를 첨부할 시 이미지는 최대20개 첨부 가능합니다.
          - 파일의 확장자는 jpg, jpeg, png, JPG, JPEG, PNG 만 가능합니다.
          """
  )
  ResponseEntity<UUID> uploadPortfolio(
      CustomOAuth2User customOAuth2User,
      PortfolioRequest request);
}
