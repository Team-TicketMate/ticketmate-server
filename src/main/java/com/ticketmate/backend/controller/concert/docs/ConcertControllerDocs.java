package com.ticketmate.backend.controller.concert.docs;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.concert.response.ConcertInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface ConcertControllerDocs {

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
                    - sortField: created_date(기본값), ticket_open_date
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
                    - sortField와 sortDirection은 해당하는 문자열만 입력 가능합니다.
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
                    - **concertId** (UUID): 공연 ID
                    - **concertName** (String): 공연 이름
                    - **concertHallName** (String): 공연장 이름
                    - **concertType** (String): 공연 유형 (예: CONCERT, MUSICAL 등)
                    - **concertDates** (List): 공연 날짜 리스트
                        - **startDateTime** (String): 공연 시작 시간 (yyyy-MM-dd HH:mm:ss)
                        - **endDateTime** (String): 공연 종료 시간 (yyyy-MM-dd HH:mm:ss)
                    - **ticketOpenDates** (List): 예매 오픈 날짜 리스트
                        - **ticketOpenDatetime** (String): 예매 오픈 시간
                        - **ticketReservationSite** (String): 예매처 (예: INTERPARK_TICKET 등)
            
                    ### 사용 방법 & 유의 사항
                    - 이 API는 로그인된 사용자만 호출할 수 있습니다.
                    - 공연 ID는 UUID 형식이어야 하며, 올바르지 않으면 오류가 발생합니다.
                    - 반환되는 날짜 및 시간 정보는 모두 ISO 8601 형식의 문자열입니다.
                    - 공연에 따라 `ticketOpenDates` 또는 `concertDates` 정보가 없을 수 있습니다.
            
                    ### 예외 처리
                    - **404 NOT_FOUND**: 해당 공연 ID에 대한 공연 정보가 존재하지 않을 경우
                        - message: "존재하지 않는 공연입니다."
                    - **400 BAD_REQUEST**: UUID 형식이 아닌 concertId를 요청 시
                        - message: "잘못된 요청입니다."
                    """
    )
    ResponseEntity<ConcertInfoResponse> getConcertInfo(
            CustomOAuth2User customOAuth2User,
            UUID concertId);
}
