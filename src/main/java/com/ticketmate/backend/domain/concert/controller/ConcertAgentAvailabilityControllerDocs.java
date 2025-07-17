package com.ticketmate.backend.domain.concert.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAcceptingAgentFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentResponse;
import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
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
  public ResponseEntity<Void> setAcceptingOption(
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
      )
  })
  @Operation(
      summary = "공연별 수락 대리인 목록 조회 (정렬 기능 추가)",
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
        - **[중요] 정렬 기능 초기 구현 안내**
          - 현재 정렬 기능은 1차 구현 단계입니다.
          - 후기, 팔로잉, 성공 등의 기능이 아직 구현되지 않아, 현재는 어떤 정렬 필드를 선택해도 **실질적으로는 등록일 순(`createdDate`)으로 정렬**됩니다.
          - 1차 정렬 기준의 점수가 모든 대리인에게 동일하여, 2차 정렬 기준인 등록일 순으로 정렬되기 때문입니다.
        
        ### 주요 오류 코드
          - **CONCERT_NOT_FOUND**: 콘서트를 찾을 수 없음
          - **MISSING_AUTH_TOKEN**: 요청에 액세스 토큰이 포함되어 있지 않음
          - **INVALID_ACCESS_TOKEN**: 액세스 토큰이 유효하지 않음
          - **EXPIRED_ACCESS_TOKEN**: 액세스 토큰이 만료됨
          - **INVALID_SORT_FIELD**: 정렬 필드 요청 잘못됨
        """
  )
  public ResponseEntity<Slice<ConcertAcceptingAgentResponse>> filteredAcceptingAgents(
      UUID concertId,
      ConcertAcceptingAgentFilteredRequest request);
}
