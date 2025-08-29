package com.ticketmate.backend.api.application.controller.search;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.search.application.dto.request.SearchRequest;
import com.ticketmate.backend.search.application.dto.response.SearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SearchControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-29",
          author = "Chuseok22",
          description = "검색 request DTO 기본값 수정",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/492"
      ),
      @ApiChangeLog(
          date = "2025-07-19",
          author = "Yooonjeong",
          description = "공연, 대리인 임베딩 기반 하이브리드 검색 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/376"
      ),
      @ApiChangeLog(
          date = "2025-08-18",
          author = "Yooonjeong",
          description = "최근 검색어 기능 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/375"
      )
  })
  @Operation(
      summary = "공연・대리인 하이브리드 검색",
      description = """
          이 API는 인증 없이 호출 가능합니다.
          키워드를 입력하여 공연/대리인의 임베딩 기반 하이브리드 검색을 수행합니다.
          **로그인한 경우에만** 서버가 최근 검색어를 저장합니다(비로그인은 클라이언트에서 관리).
          
          ### 요청 파라미터
          - **keyword** (String, Query) : 검색어 [필수]
          - **searchType** (SearchType, Query) : 검색 타입 [필수]
            - `CONCERT` : 공연
            - `AGENT`   : 대리인
          - **pageNumber** (int, Query) : 페이지 번호 [선택, 기본값 `1` ]
          - **pageSize** (int, Query) : 페이지 사이즈 [선택, 기본값 `10`]
          
          ### 반환값
          `SearchResponse<ConcertSearchResponse>` 또는 `SearchResponse<AgentSearchResponse>`
          
          - **searchResults.content** : 결과 객체 리스트
            - **ConcertSearchResponse**
              - **concertId** (UUID)          : 공연 식별자
              - **concertName** (String)        : 공연 제목
              - **concertHallName** (String)        : 공연장 이름
              - **ticketPreOpenDate** (LocalDateTime) : 선예매 오픈일 (`yyyy-MM-dd'T'HH:mm:ss`)
              - **ticketGeneralOpenDate** (LocalDateTime) : 일반 예매 오픈일 (`yyyy-MM-dd'T'HH:mm:ss`)
              - **startDate** (LocalDateTime) : 공연 시작일 (`yyyy-MM-dd'T'HH:mm:ss`)
              - **endDate** (LocalDateTime) : 공연 종료일 (`yyyy-MM-dd'T'HH:mm:ss`)
              - **concertThumbnailUrl** (String)        : 썸네일 이미지 URL
              - **score** (double)        : 검색 순위 점수
          
            - **AgentSearchResponse**
              - **agentId** (UUID)   : 대리인 식별자
              - **nickname** (String) : 닉네임
              - **profileUrl** (String) : 프로필 이미지 URL
              - **introduction** (String) : 소개 문구
              - **averageRating** (double) : 평균 별점
              - **reviewCount** (int)    : 후기 수
              - **score** (double) : 검색 순위 점수
          
          - **searchResults.first** (boolean) : 첫 페이지 여부
          - **searchResults.last** (boolean) : 마지막 페이지 여부
          - **concertCount** (Integer) : 전체 공연 결과 수 (첫 페이지에만)
          - **agentCount** (Integer) : 전체 대리인 결과 수 (첫 페이지에만)
          
          ### 사용 방법 & 유의사항
          - `keyword`, `searchType` 은 `필수` 값 입니다.
          - `pageNumber`/`pageSize`를 `null` 또는 생략 시 기본값이 적용됩니다.
          - 반환 타입이 `Slice`이므로, `first`/`last` 플래그를 보고 무한 스크롤 구현이 가능합니다.
          - **score** 필드로 결과 랭킹을 판단할 수 있습니다.
          """
  )
  ResponseEntity<SearchResponse<?>> search(SearchRequest request, CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
          @ApiChangeLog(
                  date = "2025-08-18",
                  author = "Yooonjeong",
                  description = "최근 검색어 기능 구현",
                  issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/375"
          )
  })
  @Operation(
          summary = "최근 검색어 조회",
          description = """
        이 API는 인증이 필요합니다.
        로그인한 사용자의 최근 검색어 목록을 조회합니다.
        
        ### 요청 파라미터
        - 없음
        - 인증 컨텍스트(`CustomOAuth2User`)로 현재 사용자 식별
        
        ### 응답 데이터
        - `List<String>` : 최근 검색어 문자열 배열
          - 반환 예: `["뮤지컬 햄릿", "서울 콘서트", "대리 예매"]`
          - 정렬: Redis ZSet의 **score 기준 내림차순**(최근 검색어가 먼저)
          - 개수: 서비스 내부 설정값 `maxSize` 범위 내 상위 항목만 반환
          - 최근 검색어가 없을 경우 **빈 배열(`[]`)** 반환
        
        ### 사용 방법 & 유의사항
        - 별도 파라미터 없이 **인증만** 되어 있으면 호출 가능합니다.
        - 반환 리스트는 **현재 로그인 사용자 기준**으로 개인화됩니다.
        - 무한스크롤/페이지네이션은 제공하지 않으며, 단순 상위 N개 목록입니다.
        """
  )
  ResponseEntity<List<String>> getRecentSearch(CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
          @ApiChangeLog(
                  date = "2025-08-18",
                  author = "Yooonjeong",
                  description = "최근 검색어 기능 구현",
                  issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/375"
          )
  })
  @Operation(
          summary = "최근 검색어 전체 삭제",
          description = """
        이 API는 인증이 필요합니다.
        로그인한 사용자의 최근 검색어를 모두 삭제합니다.
        
        ### 요청 파라미터
        - 없음
        - 인증 컨텍스트(`CustomOAuth2User`)로 현재 사용자 식별
        
        ### 응답 데이터
        - 본문 없음 (`Void`)
        - HTTP 204 NO CONTENT
        
        ### 사용 방법 & 유의사항
        - 대상은 **현재 로그인한 사용자**의 데이터만입니다.
        - 실제로 삭제된 데이터가 있었는지 여부와 관계없이, 요청이 성공적으로 처리되면 항상 본문 없는(No Content) 응답을 반환합니다.
        """
  )
  ResponseEntity<Void> deleteAllRecentSearch(CustomOAuth2User customOAuth2User);
}
