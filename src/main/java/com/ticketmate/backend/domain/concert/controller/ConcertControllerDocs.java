package com.ticketmate.backend.domain.concert.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.concert.domain.dto.request.ConcertFilteredRequest;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertFilteredResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertInfoResponse;
import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ConcertControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-07",
          author = "Chuseok22",
          description = "SortField 정렬 필드 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/393"
      )
  })
  @Operation(
      summary = "공연 정보 필터링",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **concertName** (String): 공연 제목 검색어 [선택]
          - **concertHallName** (String): 공연장 이름 검색어 [선택]
          - **concertType** (String): 공연 카테고리 [선택]
          - **ticketReservationSite** (String): 예매처 사이트 [선택]
          - **pageNumber** (Integer): 요청 페이지 번호 [선택]
          - **pageSize** (Integer): 한 페이지 당 항목 수 [선택]
          - **sortField** (String): 정렬할 필드 [선택]
          - **sortDirection** (String): 정렬 방향 [선택]
          
          ### 사용 방법
          `필터링 파라미터`
          - concertName: 검색어가 포함된 공연을 반환합니다
          - concertHallName: 검색어가 포함된 공연장을 반환합니다
          - concertType: 카테고리에 해당하는 공연을 반환합니다
          - ticketReservationSite: 티켓 예매처 사이트에 해당하는 공연을 반환합니다
          
          `정렬 조건`
          - sortField: CREATED_DATE(기본값), TICKET_OPEN_DATE
          - sortDirection: DESC(기본값), ASC
          
          `ConcertType`
          CONCERT
          MUSICAL
          SPORTS
          CLASSIC
          EXHIBITIONS
          OPERA
          ETC
          
          `TicketReservationSite`
          INTERPARK_TICKET
          YES24_TICKET
          TICKEK_LINK
          MELON_TICKET
          COUPANG_PLAY
          ETC
          
          ### 유의사항
          - concertName, concertHallName, concertType, ticketReservationSite 는 요청하지 않을 경우 필터링 조건에 적용되지 않습니다
          - sortField와 sortDirection은 해당하는 문자열만 입력 가능합니다. (UPPER_CASE)
          - 기존 로직은 티켓 오픈일이 지난 공연도 반환했으나, 변경된 로직에서는 **티켓 선예매/일반예매 오픈일이 모두 지난 공연은 결과에서 제외**됩니다.
          """
  )
  ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
      CustomOAuth2User customOAuth2User,
      ConcertFilteredRequest request);

  @Operation(
      summary = "공연 정보 상세조회",
      description = """
          이 API는 특정 공연 ID를 통해 공연의 상세 정보를 조회합니다.
          인증이 필요합니다.
          
          ### 요청 파라미터
          - **concertId** (UUID): 조회할 공연의 고유 ID (PathVariable)
          
          ### 응답 데이터
          - **concertName** (String): 공연 이름
          - **concertHallName** (String): 공연장 이름
          - **concertThumbnailUrl** (String): 공연 썸네일 이미지 URL
          - **seatingChartUrl** (String): 좌석 배치도 URL
          - **concertType** (String): 공연 유형 (예: CONCERT, MUSICAL 등)
          - **concertDateInfoResponse** (List): 공연 날짜 리스트
              - **performanceDate** (LocalDateTime): 공연 시작 시간 (yyyy-MM-dd'T'HH:mm:ss)
              - **session** (Integer): 회차
          - **ticketOpenDateList** (List): 예매 오픈 날짜 리스트
              - **openDate** (LocalDateTime): 티켓 오픈일
              - **requestMaxCount** (Integer): 최대 예매 매수
              - **isBankTransfer** (Boolean): 무통장 입금 여부
              - **ticketOpenType** (Enum): 선예매, 일반예매 여부
          - **ticketReservationSite** (String): 예매처 (예: INTERPARK_TICKET 등)
          
          ### 사용 방법 & 유의 사항
          - 티켓 오픈일이 이미 지난 공연의 티켓 오픈 데이터는 반화되지 않습니다
          """
  )
  ResponseEntity<ConcertInfoResponse> getConcertInfo(
      CustomOAuth2User customOAuth2User,
      UUID concertId);
}
