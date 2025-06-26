package com.ticketmate.backend.domain.member.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.dto.response.MemberInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface MemberControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-26",
          author = "Chuseok22",
          description = "회원 정보 조회 반환값 memberId(UUID) 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/360"
      ),
      @ApiChangeLog(
          date = "2025-06-24",
          author = "Yooonjeong",
          description = "회원 정보 조회 API 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/285"
      )
  })
  @Operation(
      summary = "내 정보 조회",
      description = """
          이 API는 인증이 필요합니다.
          인증된 사용자의 정보를 조회합니다.
          
          ### 반환값
          ```json
            {
              "memberId": "269e1e88-a187-4b93-b3b0-71e9003e9b22",
              "username": "email@naver.com",
              "nickname": "269e1e88-a187-4b93-b3b0-71e9003e9b22",
              "name": "장윤정",
              "birthDay": "0701",
              "birthYear": "2002",
              "phone": "010-1234-5678",
              "profileUrl": null,
              "gender": "female",
              "memberType": "CLIENT"
            }
          ```
          
          ### 유의사항
          - 이 API는 JWT 기반 인증이 필요하며, AccessToken이 유효해야 합니다.
          - 사용자는 자신의 정보만 조회할 수 있으며, 서버는 인증된 사용자 정보를 기반으로 자동 조회합니다.
          
          ### 주요 오류 코드
          - **MISSING_AUTH_TOKEN**: 요청에 액세스 토큰이 포함되어 있지 않음
          - **INVALID_ACCESS_TOKEN**: 액세스 토큰이 유효하지 않음
          - **EXPIRED_ACCESS_TOKEN**: 액세스 토큰이 만료됨
          
          **추가 설명:**
          
          - 서버는 `@AuthenticationPrincipal`을 사용해 인증된 사용자 정보를 자동 주입받습니다.
          - 인증 정보가 없거나 잘못된 경우에는 컨트롤러에 진입하지 않고 보안 필터에서 차단됩니다.
          """
  )
  public ResponseEntity<MemberInfoResponse> getMemberInfo(CustomOAuth2User customOAuth2User);
}
