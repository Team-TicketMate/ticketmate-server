package com.ticketmate.backend.domain.search.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.search.domain.dto.request.SearchRequest;
import com.ticketmate.backend.domain.search.domain.dto.response.SearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface SearchControllerDocs {
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-19",
          author = "Yooonjeong",
          description = "공연, 대리인 임베딩 기반 하이브리드 검색 구현",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/376"
      )
  })
  @Operation(
      summary = "공연・대리인 하이브리드 검색",
      description = """
이 API는 인증이 필요합니다.
키워드를 입력하여 공연/대리인의 임베딩 기반 하이브리드 검색을 수행합니다.

### 요청 파라미터
- **keyword** (String, Query) : 검색어 [필수]
- **type** (SearchType, Query)
  - `CONCERT` : 공연
  - `AGENT`   : 대리인
  - 기본값: `CONCERT`
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
    - **startDate** (LocalDateTime) : 공연 시작일 (`yyyy.MM.dd`)
    - **endDate** (LocalDateTime) : 공연 종료일 (`yyyy.MM.dd`)
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
- `type`을 생략하면 공연(`CONCERT`) 검색이 수행됩니다.
- `pageNumber`/`pageSize`를 `null` 또는 생략 시 기본값이 적용됩니다.
- 반환 타입이 `Slice`이므로, `first`/`last` 플래그를 보고 무한 스크롤 구현이 가능합니다.
- **score** 필드로 결과 랭킹을 판단할 수 있습니다.
"""
  )
  public ResponseEntity<SearchResponse<?>> search(@ParameterObject @Valid SearchRequest request);
}
