package com.ticketmate.backend.api.application.controller.member;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.member.application.dto.request.AgentSaveBankAccountRequest;
import com.ticketmate.backend.member.application.dto.request.AgentUpdateBankAccountRequest;
import com.ticketmate.backend.member.application.dto.request.MemberFollowFilteredRequest;
import com.ticketmate.backend.member.application.dto.request.MemberFollowRequest;
import com.ticketmate.backend.member.application.dto.request.MemberInfoUpdateRequest;
import com.ticketmate.backend.member.application.dto.request.MemberWithdrawRequest;
import com.ticketmate.backend.member.application.dto.response.AgentBankAccountResponse;
import com.ticketmate.backend.member.application.dto.response.MemberFollowResponse;
import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
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

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-09-29",
          author = "mr6208",
          description = "대리인 계좌등록 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/537"
      )
  })
  @Operation(
      summary = "대리인 계좌등록",
      description = """
          대리인이 자신의 계좌를 티켓메이트 서비스에 등록합니다.
          
          ### 요청 파라미터
          - bankCode(String) : 계좌의 은행 정보(필수)
          - accountHolder(String) : 예금주 명(필수, 최대 20자)
          - accountNumber(String) : 계좌번호(필수, 11~16자)
          - primaryAccount(boolean) : 대표계좌 유/무(필수)
          
          ### 사용 방법
          `BankCode`
          
          - KYONGNAM_BANK("039", "경남"),
          - GWANGJU_BANK("034", "광주"),
          - LOCALNONGHYEOP("012", "지역축농협"),
          - BUSAN_BANK("032", "부산"),
          - SAEMAUL("045", "새마을"),
          - SANLIM("064", "산림"),
          - SHINHAN("088", "신한"),
          - SHINHYEOP("048", "신협"),
          - CITI("027", "씨티"),
          - WOORI("020", "우리"),
          - POST("071", "우체국"),
          - SAVING_BANK("050", "저축"),
          - JEONBUK_BANK("037", "전북"),
          - JEJU_BANK("035", "제주"),
          - KAKAO_BANK("090", "카카오"),
          - K_BANK("089", "케이"),
          - TOSS_BANK("092", "토스"),
          - HANA("081", "하나"),
          - HSBC("054", "홍콩상하이"),
          - IBK("003", "기업"),
          - KOOKMIN("004", "국민"),
          - DAEGU_BANK("031", "대구"),
          - KDB_BANK("002", "산업"),
          - NONGHYEOP("011", "농협"),
          - SC("023", "SC제일"),
          - SUHYEOP("007", "수협");
          
          ### 유의 사항
          - BankCode의 파라미터는 각각 금융결제원 공식 코드, 보여주기용 문자입니다.
          - 계좌번호 입력 시 **'-' 문자는 제외하고 호출해야 합니다.**
          - 만약 기존에 대표계좌로 설정된 계좌가 있고 해당 API를 호출하며 등록하는 계좌를 대표계좌로 설정할 시 기존 대표계좌는 대표계좌에서 강등됩니다.
          """
  )
  ResponseEntity<Void> saveBankAccount(CustomOAuth2User customOAuth2User, AgentSaveBankAccountRequest request
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-09-30",
          author = "mr6208",
          description = "대리인 계좌조회 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/537"
      )
  })
  @Operation(
      summary = "대리인 계좌조회",
      description = """
          대리인이 자신의 계좌를 조회합니다.
          
          ### 요청 파라미터 X (인증 필수)
          
          ### 유의 사항
          - 자신의 계좌중 대표계좌가 최상단에 배치되며 나머지는 생성일자 기준으로 정렬됩니다. 
          - 암호화된 계좌번호를 평문으로 클라이언트에게 전송합니다.
          """
  )
  ResponseEntity<List<AgentBankAccountResponse>> getBankAccountList(CustomOAuth2User customOAuth2User
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-09-30",
          author = "mr6208",
          description = "대리인 대표계좌 변경 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/537"
      )
  })
  @Operation(
      summary = "대리인 대표계좌 변경",
      description = """
          대리인이 자신의 대표계좌를 변경합니다.
          
          ### 요청 파라미터
          - bank-account-id : 변경할 계좌 ID
          
          ### 유의 사항
          - 대표계좌 변경시 순차적으로 모든 계좌들의 대표계좌 필드가 false로 변경된 이후에 변경할 계좌의 필드가 ture로 변경됩니다.  
          - 대표계좌 변경은 총 2개 이상일때만 가능하도록 설계해놨습니다. (방어로직은 X)
          - 처음으로 계좌 생성 시 반드시 대표계좌로 설정되도록 설계 + 계좌 삭제 시에 남은 계좌의 수가 1개일 시 반드시 남은 계좌가 대표계좌로 변경되도록 설계. 
          """
  )
  ResponseEntity<Void> changePrimaryBankAccount(UUID agentBankAccountId, CustomOAuth2User customOAuth2User
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-09-30",
          author = "mr6208",
          description = "대리인 대표계좌 수정 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/537"
      )
  })
  @Operation(
      summary = "대리인 대표계좌 수정",
      description = """
          대리인이 자신의 대표계좌를 수정합니다.
          
          ### 요청 파라미터 (AgentUpdateBankAccountRequest)
          - bankCode(String) : 계좌의 은행 정보(필수X)
          - accountHolder(String) : 예금주 명(필수X, 최대 20자)
          - accountNumber(String) : 계좌번호(필수X, 11~16자)
          
          
          ### 유의 사항
          - 모든 필드가 필수가 아니라 아닌 변경하고싶은 필드만 채워넣습니다. (Null 허용)   
          - 만약 Null이 아닌 필드는 변경하고싶은 필드라고 판단, DTO단에서 검증이 들어갑니다.
          - 대표계좌 변경은 API를 분기하였습니다. 
          """
  )
  ResponseEntity<Void> changeBankAccountInfo(UUID agentBankAccountId, CustomOAuth2User customOAuth2User, AgentUpdateBankAccountRequest request
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-02",
          author = "mr6208",
          description = "대리인 대표계좌 삭제 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/537"
      )
  })
  @Operation(
      summary = "대리인 대표계좌 삭제",
      description = """
          대리인이 자신의 대표계좌를 삭제합니다.
          
          ### 요청 파라미터
          - bank-account-id : 삭제할 계좌 ID
          
          
          ### 유의 사항
          - 논리삭제가 아닌 물리삭제를 진행합니다. (데이터는 어차피 인당 최대 5개)
          - 만약 삭제 후 남은 계좌의 개수가 1개라면 그 계좌는 자동으로 대표계좌로 설정됩니다.
          """
  )
  ResponseEntity<Void> deleteBankAccount(
      UUID agentBankAccountId,
      CustomOAuth2User customOAuth2User
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-10-29",
          author = "chuseok22",
          description = "회원 탈퇴 기능 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/581"
      )
  })
  @Operation(
      summary = "회원 탈퇴",
      description = """
          ### 요청 파라미터 (Request Body)
          - `withdrawalReasonType` (WithdrawalReasonType, required): 탈퇴 사유 타입
            - 가능한 값:
              - `NO_CONCERTS` ("찾는 공연이 없어요")
              - `RUDE_USER` ("비매너 사용자를 만났어요")
              - `UNFAIR_RESTRICTION` ("억울하게 이용이 제한됐어요")
              - `WANT_NEW_ACCOUNT` ("새 계정을 만들고 싶어요")
              - `DELETE_PERSONAL_DATA` ("개인정보를 삭제하고 싶어요")
              - `OTHER` ("기타")
          - `otherReason` (String, optional, 최대 20자): 기타 사유 입력란
            - `withdrawalReasonType`이 `OTHER`인 경우에만 처리 대상입니다.
            - 저장 전 **특수문자 제거 및 정규화**가 적용되며, 정규화된 문자열 기준으로 최대 20자까지만 저장됩니다.
            - `OTHER`가 아닌 경우 이 값은 **무시**되며 저장 시 `null`로 처리됩니다.
          
          #### 요청 예시
          - 사유 선택형:
            ```json
            {
              "withdrawalReasonType": "NO_CONCERTS"
            }
            ```
          - 기타 사유 입력:
            ```json
            {
              "withdrawalReasonType": "OTHER",
              "otherReason": "티켓팅이 불편해서"
            }
            ```
          
          ### 응답 데이터
          `없음`
          
          ### 사용 방법
          1. 화면에서 탈퇴 사유를 선택합니다.
             - 사유가 `OTHER`인 경우에만 `otherReason` 입력란을 노출하여 함께 전달합니다.
          2. 위의 Request Body 형식으로 JSON을 전송합니다.
          3. 서버는 다음을 수행합니다.
             - 탈퇴 이력 저장 (회원ID, 전화번호, 닉네임, 사유 타입, 기타 사유)
             - 해당 **전화번호를 탈퇴 사유(WITHDRAWAL)로 차단**
          4. 응답은 컨트롤러 구현에 따릅니다.
          
          ### 유의 사항
          - `withdrawalReasonType`은 **필수**입니다.
          - `otherReason`은 `withdrawalReasonType`이 `OTHER`일 때만 의미가 있으며,
            저장 전 **특수문자 제거/정규화** 후 **최대 20자**까지만 저장됩니다.
          - 탈퇴 처리 시, 해당 **전화번호는 30일간 차단**됩니다. (BlockType: `WITHDRAWAL`)
          - 코드 상 주석 기준으로, **회원 논리 삭제 로직은 아직 미구현** 상태입니다. (TODO)
          """
  )
  ResponseEntity<Void> withdraw(
      CustomOAuth2User customOAuth2User,
      MemberWithdrawRequest request
  );
}
