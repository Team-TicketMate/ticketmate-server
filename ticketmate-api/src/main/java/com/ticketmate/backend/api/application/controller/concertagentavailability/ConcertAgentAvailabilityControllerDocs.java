package com.ticketmate.backend.api.application.controller.concertagentavailability;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.concertagentavailability.application.dto.request.AgentConcertSettingFilteredRequest;
import com.ticketmate.backend.concertagentavailability.application.dto.request.ConcertAcceptingAgentFilteredRequest;
import com.ticketmate.backend.concertagentavailability.application.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentAcceptingConcertResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.AgentConcertSettingResponse;
import com.ticketmate.backend.concertagentavailability.application.dto.response.ConcertAcceptingAgentResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

public interface ConcertAgentAvailabilityControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-28",
          author = "Yooonjeong",
          description = "공연별 대리인 ON/OFF 전환 API 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/249"
      )
  })
  @Operation(
      summary = "공연 대리인 수락 옵션 설정",
      description = """
            이 API는 인증이 필요합니다.\s
            대리인 수락 여부 및 한줄소개 정보를 설정합니다.
          
            ### 요청 파라미터
            - **concertId** (UUID): 공연 식별자 [필수]
            - **accepting** (boolean): 수락 여부 [선택 (누락 시 true)]
            - **introduction** (String): 소개 문구 [선택]
          
            ### 반환값
            (응답 본문 없음)
          
            ### 유의사항
            - 인증된 사용자만 호출할 수 있습니다.
            - **accepting**이 **false**일 경우 **introduction** 필드는 무시되고 null로 처리됩니다.
            - Validation 실패 시 400 Bad Request가 반환됩니다.
          
            ### 주요 오류 코드
            - **CONCERT_NOT_FOUND**: 콘서트를 찾을 수 없음
            - **MISSING_AUTH_TOKEN**: 요청에 액세스 토큰이 포함되어 있지 않음
            - **INVALID_ACCESS_TOKEN**: 액세스 토큰이 유효하지 않음
            - **EXPIRED_ACCESS_TOKEN**: 액세스 토큰이 만료됨
          
            **추가 설명:**
            - **@AuthenticationPrincipal**으로 인증된 사용자 정보를 확인합니다.
            - 인증 정보가 없거나 잘못된 경우 보안 필터에서 차단됩니다.
          """
  )
  ResponseEntity<Void> setAcceptingOption(
      CustomOAuth2User customOAuth2User,
      ConcertAgentAvailabilityRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-28",
          author = "Yooonjeong",
          description = "공연별 ON 대리인 조회 API 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/249"
      ),
      @ApiChangeLog(
          date = "2025-06-30",
          author = "Yooonjeong",
          description = "대리인 조회 페이지네이션 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/249"
      ),
      @ApiChangeLog(
          date = "2025-07-07",
          author = "Chuseok22",
          description = "SortField 정렬 필드 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/393"
      ),
      @ApiChangeLog(
          date = "2025-07-11",
          author = "Yooonjeong",
          description = "대리인 목록 정렬 기능 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/374"
      ),
      @ApiChangeLog(
        date = "2025-12-02",
        author = "Yooonjeong",
        description = "대리인 성공 순 및 총점(AI 추천순) 정렬 기능 추가",
        issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/641"
      )
  })
  @Operation(
      summary = "공연별 수락 대리인 목록 조회 (정렬 기능 포함)",
      description = """
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **concert-id (UUID, Path)** : 조회할 공연 식별자 [필수]
          - **ConcertAcceptingAgentFilteredRequest (Query)**
            - **pageNumber (int)** : 요청할 페이지 번호 [선택, 기본값 1]
            - **pageSize (int)** : 요청할 페이지 사이즈 [선택, 기본값 10]
            - **sortField (String)** : 정렬 필드 [선택, 기본값 TOTAL_SCORE]
              - **사용 가능 값**: `TOTAL_SCORE`(기본 추천순), `AVERAGE_RATING`(별점순), `REVIEW_COUNT`(후기순), `FOLLOWER_COUNT`(팔로워순), `RECENT_SUCCESS_COUNT`(최근 성공순)
            - **sortDirection (String)** : 정렬 방향 [선택, 기본값 DESC]
          
          ### 반환값 [Slice<ConcertAcceptingAgentResponse>]
          - **content** : 대리인 정보 리스트
            - **agentId** (UUID)       : 대리인 식별자
            - **nickname** (String)    : 닉네임
            - **profileUrl** (String)  : 프로필 이미지 URL
            - **introduction** (String): 소개 문구 (없으면 빈 문자열)
            - **averageRating** (double): 평균 별점
            - **reviewCount** (int)    : 후기 수
          - **first** (boolean) : 첫 페이지 여부
          - **last** (boolean)  : 마지막 페이지 여부
          
          ### 유의사항
          - **pageNumber** 기본값은 **1**, **pageSize** 기본값은 **10** 입니다.
          - 페이지네이션 파라미터를 **null**로 전송할 경우, 기본값이 적용됩니다.
          - Slice 타입을 사용하므로 클라이언트는 **first**, **last** 플래그를 보고 무한 스크롤을 구현할 수 있습니다.
          - **introduction** 필드는 **null**이 아닌 빈 문자열(**""**)로 반환됩니다.
          - **`TOTAL_SCORE` (AI 추천)** 정렬 기능이 적용되었습니다.
            - 총점은 후기, 팔로워, 별점, 최근 성공 수를 기반으로 **매일 새벽 4시** 및 애플리케이션 시작 시를 통해 미리 계산되어 DB에 저장됩니다.
            - **총점 계산 공식**:
                * **리뷰**: `sqrt(후기수) * 품질 점수 * 배수`
                * **팔로워**: `log(1 + 팔로워수) * 배수`
                * **별점**: `평균별점 * 배수`
                * **성공**: `최근 30일 성공 수 * 배수`
            - 다른 정렬 필드(`AVERAGE_RATING`, `REVIEW_COUNT` 등)도 해당 지표를 기준으로 정렬됩니다.
          
          ### 주요 오류 코드
            - **CONCERT_NOT_FOUND**: 콘서트를 찾을 수 없음
            - **MISSING_AUTH_TOKEN**: 요청에 액세스 토큰이 포함되어 있지 않음
            - **INVALID_ACCESS_TOKEN**: 액세스 토큰이 유효하지 않음
            - **EXPIRED_ACCESS_TOKEN**: 액세스 토큰이 만료됨
            - **INVALID_SORT_FIELD**: 정렬 필드 요청 잘못됨
          """
  )
  ResponseEntity<Slice<ConcertAcceptingAgentResponse>> filteredAcceptingAgents(
      UUID concertId,
      ConcertAcceptingAgentFilteredRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-11-01",
          author = "Yooonjeong",
          description = "대리인 수락 on/off 포함한 공연 리스트 API 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/566"
      )
  })
  @Operation(
      summary = "대리인 on/off 설정을 위한 공연 목록 조회",
      description = """
        ### 요청 파라미터
        - **pageNumber (int)** : 요청할 페이지 번호 [선택, 기본값 1]
        - **pageSize (int)** : 요청할 페이지 사이즈 [선택, 기본값 10]

        ### 응답 데이터
        - Slice<AgentConcertSettingResponse>
        - **content** : 공연 리스트 (대리인 on/off 설정 여부, 공연 모집 상태, 매칭된 의뢰인 수 포함)
          - **concertId** (UUID): 공연 PK
          - **concertName** (String): 공연 제목
          - **concertThumbnailUrl** (String): 공연 썸네일 공개 URL
          - **matchedClientCount** (int): 해당 대리인과 매칭된 의뢰인 수 (신청서 **APPROVED** 상태)
          - **accepting** (boolean): 대리인의 on/off 설정 값

        ### 사용 방법
        1) 인증 후 호출 예: GET /concerts?pageNumber=1&pageSize=10
        2) 이 목록에는 **현재 모집 중**인 모든 공연이 제공됩니다.
        3) **accepting** 기준으로 **true** 우선, **false** 후순위로 정렬해 반환하므로, 클라이언트는 **accepting** 값으로 접수중/마감을 구분해 표시할 수 있습니다.
        4) Slice 타입을 사용하므로 클라이언트는 **first**, **last** 플래그를 보고 무한 스크롤을 구현할 수 있습니다.

        ### 정렬/필터 동작
        - 정렬 우선순위:
          1. 대리인 설정(accepting) 내림차순 → **ON(true)** 먼저, **OFF(false)** 나중
          2. 공연 생성일(createdDate) 내림차순 → 최신순
        - **matchedClientCount**: 현재 대리인의 해당 공연에서 **APPROVED** 상태 신청서 개수
        - **accepting**: 대리인이 해당 공연에 대한 신청을 받을지에 대한 on/off 설정

        ### 예외처리
        - 400 Bad Request (검증 오류)
          - **pageNumber** < 1: "페이지 번호는 1이상 값을 입력해야합니다."
          - **pageSize** < 1: "페이지 당 데이터 최솟값은 1개 입니다."
        """
  )
  ResponseEntity<Slice<AgentConcertSettingResponse>> findConcertsForAgentAcceptingSetting(
      CustomOAuth2User customOAuth2User,
      AgentConcertSettingFilteredRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-11-01",
          author = "Yooonjeong",
          description = "대리인 수락 on/off 포함한 공연 리스트 API 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/566"
      ),
      @ApiChangeLog(
          date = "2025-11-10",
          author = "Yooonjeong",
          description = "반환 타입 Slice→List 변경 및 중복 방지 유니크 제약 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/611"
      )
  })
  @Operation(
      summary = "대리인 ON 설정된 모집 중 공연 목록 조회",
      description = """
        ### 요청 파라미터
        - 없음
        
        ### 응답 데이터
        - List<AgentAcceptingConcertResponse>
        - **content** : 대리인이 on 설정한 모집 중인 공연 리스트 (매칭된 의뢰인 수 포함)
          - **concertId** (UUID): 공연 PK
          - **concertName** (String): 공연 제목
          - **concertThumbnailUrl** (String): 공연 썸네일 공개 URL
          - **matchedClientCount** (int): 해당 대리인과 매칭된 의뢰인 수 (신청서 **APPROVED** 상태)

        ### 사용 방법
        1) 인증 후 호출 예: GET /accepting-concerts
        2) 이 목록에는 **대리인이 ON으로 설정**했고 **현재 모집 중**인 공연만 포함됩니다.
        3) 최대 사이즈가 10인 정렬된 List가 반환됩니다.

        ### 필터/정렬 동작
        - 필터: 모집 중(**OPEN**)이면서, 현재 로그인한 대리인이 ON 설정한 공연만 필터링
        - 정렬:
          - **createdDate** 내림차순 (최신순)
        - **matchedClientCount**: 해당 대리인 + 해당 공연에서 **APPROVED** 신청서 개수

        ### 예외처리
        - 400 Bad Request (검증 오류)
          - **pageNumber** < 1: "페이지 번호는 1이상 값을 입력해야합니다."
          - **pageSize** < 1: "페이지 당 데이터 최솟값은 1개 입니다."
        """
  )
  ResponseEntity<List<AgentAcceptingConcertResponse>> findAcceptingConcertByAgent(CustomOAuth2User customOAuth2User);
}
