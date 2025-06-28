package com.ticketmate.backend.domain.concert.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertAgentAvailabilityRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertAcceptingAgentInfo;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
            - **accepting** (boolean): 수락 여부 [선택 (누락 시 false)]
            - **introduction** (String): 소개 문구 [선택]

            ### 반환값
            (응답 본문 없음)

            ### 유의사항
            - 인증된 사용자만 호출할 수 있습니다.
            - `accepting`이 `false`일 경우 `introduction` 필드는 무시되고 null로 처리됩니다.
            - Validation 실패 시 400 Bad Request가 반환됩니다.

            ### 주요 오류 코드
            - **CONCERT_NOT_FOUND**: 콘서트를 찾을 수 없음
            - **MISSING_AUTH_TOKEN**: 요청에 액세스 토큰이 포함되어 있지 않음
            - **INVALID_ACCESS_TOKEN**: 액세스 토큰이 유효하지 않음
            - **EXPIRED_ACCESS_TOKEN**: 액세스 토큰이 만료됨

            **추가 설명:**
            - `@AuthenticationPrincipal`으로 인증된 사용자 정보를 확인합니다.
            - 인증 정보가 없거나 잘못된 경우 보안 필터에서 차단됩니다.
          """
  )
  public ResponseEntity<Void> setAcceptingOption(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                              @Valid @RequestBody ConcertAgentAvailabilityRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-06-28",
          author = "Yooonjeong",
          description = "공연별 ON 대리인 조회 API 추가",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/249"
      )
  })
  @Operation(
      summary = "공연별 수락 대리인 목록 조회",
      description = """
      이 API는 인증이 필요합니다.\s
      특정 공연에 대해 수락 상태인 대리인 목록을 조회합니다.

      ### 요청 파라미터
      - **concert-id** (UUID): 조회할 공연 식별자 [필수]

      ### 반환값
      - List<ConcertAcceptingAgentInfo>
        - **agentId** (UUID): 대리인 식별자
        - **nickname** (String): 닉네임
        - **profileUrl** (String): 프로필 이미지 URL
        - **introduction** (String): 소개 문구 (없으면 빈 문자열)

      ### 유의사항
      - `introduction` 필드는 null이 아닌 빈 문자열로 반환됩니다.

      ### 주요 오류 코드
      - **CONCERT_NOT_FOUND**: 콘서트를 찾을 수 없음
      - **MISSING_AUTH_TOKEN**: 요청에 액세스 토큰이 포함되어 있지 않음
      - **INVALID_ACCESS_TOKEN**: 액세스 토큰이 유효하지 않음
      - **EXPIRED_ACCESS_TOKEN**: 액세스 토큰이 만료됨
    """
  )
  public ResponseEntity<List<ConcertAcceptingAgentInfo>> getAcceptingAgents(@PathVariable(value = "concert-id") UUID concertId);
}
