package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ConcertControllerDocs {

    @Operation(
            summary = "공연 정보 필터링",
            description = """
                                        
                    이 API는 인증이 필요합니다.

                    ### 요청 파라미터
                    - **concertName** (String): 공연 제목 검색어 [선택]
                    - **concertHallName** (String): 공연장 이름 검색어 [선택]
                    - **concertType** (String): 공연 카테고리 [선택]
                    - **ticketPreOpenStartDate** (LocalDateTime): 선예매 오픈일 시작 범위 [선택]
                    - **ticketPreOpenEndDate** (LocalDateTime): 선예매 오픈일 종료 범위 [선택]
                    - **ticketOpenStartDate** (LocalDateTime): 티켓 예매 오픈일 시작 범위 [선택]
                    - **ticketOpenEndDate** (LocalDateTime): 티켓 예매 오픈일 종료 범위 [선택]
                    - **session** (Integer): 회차 [선택]
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
                    - ticketPreOpenStartDate, ticketPreOpenEndDate: 선예매 오픈일이 범위에 포함되는 공연을 반환합니다 (범위 검색)
                    - ticketOpenStartDate, ticketOpenEndDate: 티켓 예매 오픈일이 범위에 포함되는 공연을 반환합니다 (범위 검색)
                    - session: 회차가 일치하는 공연을 반환합니다
                    - ticketReservationSite: 티켓 예매처 사이트에 해당하는 공연을 반환합니다
                    
                    `정렬 조건`
                    - sortField: created_date(기본값), ticket_pre_open_date, ticket_open_date, duration
                    - sortDirection: ASC, DESC(기본값)
                    
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
                                
                    ### 유의사항
                    - concertName, concertHallName, concertType, ticketPreOpenStartDate, ticketPreOpenEndDate, ticketOpenStartDate, ticketOpenEndDate, session, ticketReservationSite 는 요청하지 않을 경우 필터링 조건에 적용되지 않습니다
                    - sortField, sortType은 해당하는 문자열만 입력 가능합니다.
                    """
    )
    ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
            CustomUserDetails customUserDetails,
            ConcertFilteredRequest request);
}
