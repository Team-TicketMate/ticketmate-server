package com.ticketmate.backend.domain.concerthall.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.concerthall.domain.dto.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallInfoResponse;
import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ConcertHallControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-07",
          author = "Chuseok22",
          description = "SortField 정렬 필드 리팩토링",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/393"
      )
  })
  @Operation(
      summary = "공연장 정보 필터링",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **concertHallName** (String): 공연장 이름 검색어 [선택]
          - **cityCode** (Integer): 지역 코드 [선택]
          - **pageNumber** (Integer): 요청 페이지 번호 [선택]
          - **pageSize** (Integer): 한 페이지 당 항목 수 [선택]
          - **sortField** (String): 정렬할 필드 [선택]
          - **sortDirection** (String): 정렬 방향 [선택]
          
          ### 사용 방법
          `필터링 파라미터`
          - concertHallName: 검색어가 포함된 공연장을 반환합니다
          - cityCode: 지역 코드에 해당하는 공연장을 반환합니다
          
          `정렬 조건`
          - sortField: CREATED_DATE(기본값)
          - sortDirection: ASC, DESC(기본값)
          
          `City`
          SEOUL 11
          BUSAN 26
          DAEGU 27
          INCHEON 28
          GWANGJU 29
          DAEJEON 30
          ULSAN 31
          SEJONG 36
          GYEONGGI 41
          GANGWON 42
          CHUNGCHEONG_BUK 43
          CHUNGCHEONG_NAM 44
          JEOLLA_BUK 45
          JEOLLA_NAM 46
          GYEONGSANG_BUK 47
          GYEONGSANG_NAM 48
          JEJU 50
          
          ### 유의사항
          - concertHallName, cityCode 는 요청하지 않을 경우 필터링 조건에 적용되지 않습니다
          - sortField, sortType은 해당하는 문자열만 입력 가능합니다. (UPPER_CASE)
          """
  )
  ResponseEntity<Page<ConcertHallFilteredResponse>> filteredConcertHall(
      CustomOAuth2User customOAuth2User,
      ConcertHallFilteredRequest request);

  @Operation(
      summary = "공연장 정보 상세조회",
      description = """
          
          이 API는 인증이 필요합니다.
          
          ### 요청 파라미터
          - **concertHallId** (UUID): 공연장 PK [필수]
          
          ### 유의사항
          
          """
  )
  ResponseEntity<ConcertHallInfoResponse> getConcertHallInfo(
      CustomOAuth2User customOAuth2User,
      UUID concertHallId);
}
