package com.ticketmate.backend.api.application.controller.member;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.member.application.dto.request.MemberFollowFilteredRequest;
import com.ticketmate.backend.member.application.dto.request.MemberFollowRequest;
import com.ticketmate.backend.member.application.dto.request.MemberInfoUpdateRequest;
import com.ticketmate.backend.member.application.dto.response.MemberFollowResponse;
import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.Slice;
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
  ResponseEntity<MemberInfoResponse> getMemberInfo(CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-09-25",
          author = "Chuseok22",
          description = "회원 정보 수정",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/538"
      )
  })
  @Operation(
      summary = "회원 정보 수정 (닉네임/프로필 이미지/한줄소개)",
      description = """
          이 API는 인증된 사용자만 사용 가능합니다
          
          **HTTP**
          - `PATCH /api/member`
          - `Content-Type: multipart/form-data`
          
          ### 요청 파라미터 (multipart/form-data)
          - `nickname` (text, optional): 변경할 닉네임. 비어있거나 공백만 있는 경우 무시됩니다.
          - `profileImg` (file, optional): 변경할 프로필 이미지 파일(바이너리). 파일이 전달되면 기존 이미지가 삭제된 후 새 파일로 교체됩니다.
          - `introduction` (text, optional): 변경할 한줄 소개. 비어있거나 공백만 있는 경우 무시됩니다. **최대 20자**.
          
          > 서버 동작 요약
          > - 닉네임: 값이 비어있지 않을 때만 `member.nickname` 갱신  
          > - 프로필 이미지: 파일이 전달되면 기존 파일 삭제 → 새 파일 업로드 → 업로드 결과의 `storedPath`로 `member.profileImgStoredPath` 갱신  
          > - 한줄 소개: 값이 비어있지 않을 때만 `member.introduction` 갱신  
          > - **제공되지 않은 필드는 변경되지 않습니다.**
          
          ### 응답 데이터
          - 본문 없음 (`ResponseEntity<Void>`)
          
          ### 사용 방법
          1. **변경하려는 필드만** multipart 폼으로 전송합니다.
          2. `nickname`, `introduction`이 빈 문자열("")이거나 공백만 있는 경우 서버가 **변경을 무시**합니다.
          3. `profileImg`가 포함되면 **기존 프로필 이미지는 삭제**된 뒤 새 파일로 교체됩니다.
          
          ### 유의 사항
          - `introduction`은 DB 스키마 기준 **최대 20자**입니다.
          - 부분 업데이트(Partial Update) 방식으로, 누락된 필드는 기존 값이 유지됩니다.
          
          ### 요청 예시
          **1) 파일 포함(닉네임+한줄소개+이미지 교체)**
          ```bash
          curl -X PATCH "https://{host}/api/member" \
            -H "Content-Type: multipart/form-data" \
            -F "nickname=ticketmate_fan" \
            -F "introduction=안녕하세요!" \
            -F "profileImg=@/path/to/new-profile.jpg"
          ```
          
          **2) 파일 없이 텍스트만 수정(닉네임만 변경)**
          ```bash
          curl -X PATCH "https://{host}/api/member" \
            -H "Content-Type: multipart/form-data" \
            -F "nickname=ticketmate_fan"
          ```
          """
  )
  ResponseEntity<Void> updateMemberInfo(
      CustomOAuth2User customOAuth2User,
      MemberInfoUpdateRequest request
  );

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
  ResponseEntity<Void> follow(
      CustomOAuth2User customOAuth2User,
      MemberFollowRequest request);

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
  ResponseEntity<Void> unfollow(
      CustomOAuth2User customOAuth2User,
      MemberFollowRequest request);

  @Operation(
      summary = "팔로우 리스트 필터링 조회",
      description = """
          의뢰인(client-id)이 '팔로우하고 있는' 대리인 목록을 조회합니다.
          
          ### 엔드포인트
          - `GET /api/member/follow/{client-id}`
          - `client-id`는 **PathVariable(UUID)** 입니다.
          
          ### 요청 파라미터
          - `pageNumber` (Integer, optional, default **1**)
            - 유효성: 최소 1
          - `pageSize` (Integer, optional, default **PageableConstants.DEFAULT_PAGE_SIZE**)
            - 유효성: 최소 1, 최대 **PageableConstants.MAX_PAGE_SIZE**
          - `sortField` (MemberFollowSortField, optional, default **CREATED_DATE**)
            - 허용 값: `CREATED_DATE`, `FOLLOWER_COUNT`
            - `CREATED_DATE`는 팔로우 관계의 생성 시점을 기준으로 정렬
            - `FOLLOWER_COUNT`는 팔로우 대상 회원(=followee)의 총 팔로워 수를 기준으로 정렬
          - `sortDirection` (Sort.Direction, optional, default **DESC**)
            - 허용 값: `ASC`, `DESC`
          
          ### 응답 데이터
          - 응답 형식: `Slice<MemberFollowResponse>`
          - 콘텐츠 항목(`MemberFollowResponse`)은 아래 정보를 포함합니다.
            - `nickname` : 팔로우 대상 회원의 닉네임
            - `profileUrl` : 팔로우 대상 회원의 프로필 이미지 URL
            - `followerCount` : 팔로우 대상 회원의 총 팔로워 수
          - ※ `Slice<...>`는 페이지 끝 여부 등 슬라이싱 정보와 함께 `MemberFollowResponse` 항목들의 모음으로 반환됩니다. 실제 직렬화 구조는 서버의 공통 응답 설정을 따릅니다.
          
          ### 요청 예시
          - 생성일 최신순(기본값) 1페이지 조회
            - `GET /api/member/follow/{client-id}?pageNumber=1&pageSize=5&sortField=CREATED_DATE&sortDirection=DESC`
          - 팔로워 수 내림차순 정렬
            - `GET /api/member/follow/{client-id}?pageNumber=2&pageSize=5&sortField=FOLLOWER_COUNT&sortDirection=DESC`
          
          ### 유의 사항
          - `pageNumber`는 **1부터 시작**합니다.
          - `pageSize`는 서버 정책상 최대치(**PageableConstants.MAX_PAGE_SIZE**)를 초과할 수 없습니다.
          - `sortField=FOLLOWER_COUNT`를 사용하면, 정렬 기준은 **팔로우 대상 회원(AGENT)의 `followerCount`**입니다.
          - 본 API는 `client-id`에 해당하는 회원이 **팔로우하고 있는** 사용자들만을 반환합니다(팔로워 목록이 아님).
          """
  )
  ResponseEntity<Slice<MemberFollowResponse>> filteredMemberFollow(
      UUID clientId,
      MemberFollowFilteredRequest request
  );
}
