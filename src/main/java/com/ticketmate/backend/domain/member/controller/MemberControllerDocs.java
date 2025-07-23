package com.ticketmate.backend.domain.member.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.dto.request.FollowRequest;
import com.ticketmate.backend.domain.member.domain.dto.response.MemberInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface MemberControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-18",
          author = "Chuseok22",
          description = "회원 정보 조회 팔로우/팔로잉 수 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/356"
      ),
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
              "memberType": "CLIENT",
              "followingCount": "130",
              "followerCount": "70"
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
  public ResponseEntity<MemberInfoResponse> getMemberInfo(
      CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-18",
          author = "Chuseok22",
          description = "팔로우 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/356"
      )
  })
  @Operation(
      summary = "팔로우 기능",
      description = """
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **followeeId** (UUID): 팔로우 하려는 대상 회원 PK [필수]
          
          ### 응답 데이터
          - HTTP 200 OK
          
          **요청 예시**
          ```
          POST /api/member/follow
          Authorization: Bearer eyJ...
          Content-Type: application/json
          
          {
           "followeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
          }
          ```
          
          ### 유의 사항
          - 본인 자신(self)을 대상으로 팔로우할 수 없습니다.
          - 의뢰인(Client)이 대리인(Agent)을 팔로우하는 경우만 가능합니다.
          - 이미 팔로우한 대상에 대해 중복 호출 시 에러가 발생합니다.
          """
  )
  public ResponseEntity<Void> follow(
      CustomOAuth2User customOAuth2User,
      FollowRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-18",
          author = "Chuseok22",
          description = "언팔로우 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/356"
      )
  })
  @Operation(
      summary = "언팔로우 기능",
      description = """
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **followeeId** (UUID): 언팔로우 하려는 대상 회원 PK [필수]
          
          ### 응답 데이터
          - HTTP 200 OK
          
          ### 사용 방법
          
          **요청 예시**
          ```
          POST /api/member/unfollow
          Authorization: Bearer eyJ...
          Content-Type: application/json
          
          {
           "followeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
          }
          ```
          
          ### 유의 사항
          - 본인 자신(self)을 대상으로 언팔로우할 수 없습니다.
          - 의뢰인(Client)이 대리인(Agent)을 언팔로우하는 경우만 가능합니다.
          - 팔로우하지 않은 대상에 대해 언팔로우 호출 시 에러가 발생합니다.
          """
  )
  public ResponseEntity<Void> unfollow(
      CustomOAuth2User customOAuth2User,
      FollowRequest request);
}
