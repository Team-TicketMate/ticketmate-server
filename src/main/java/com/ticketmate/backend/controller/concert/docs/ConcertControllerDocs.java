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

                    ### 요청 파라미터
                    - **concertId** (UUID): 공연 PK [필수]
    
                    ### 응답 데이터
                    - **concertName** (String): 공연명
                    - **concertHallName** (String): 공연장 이름 (없을 경우 null)
                    - **concertThumbnailUrl** (String): 공연 썸네일 이미지 URL
                    - **seatingChartUrl** (String): 좌석 배치도 URL (없을 경우 null)
                    - **concertType** (String): 공연 카테고리 (없을 경우 null)
                    - **startDate** (LocalDateTime): 공연 시작 일자 (가장 빠른 공연 날짜)
                    - **endDate** (LocalDateTime): 공연 종료 일자 (가장 늦은 공연 날짜)
                    - **preOpenDate** (LocalDateTime): 선예매 오픈일 (없을 경우 null)
                    - **preOpenRequestMaxCount** (Integer): 선예매 최대 예매 매수 (없을 경우 null)
                    - **preOpenIsBankTransfer** (Boolean): 선예매 무통장 입금 가능 여부 (없을 경우 null)
                    - **generalOpenDate** (LocalDateTime): 일반 예매 오픈일 (없을 경우 null)
                    - **generalOpenRequestMaxCount** (Integer): 일반 예매 최대 예매 매수 (없을 경우 null)
                    - **generalOpenIsBankTransfer** (Boolean): 일반 예매 무통장 입금 가능 여부 (없을 경우 null)
                    - **ticketReservationSite** (String): 예매처 (없을 경우 null)
    
                    ### 사용 방법
                    - concertId를 통해 특정 공연의 상세 정보를 조회합니다.
                    - 공연 날짜는 여러 개일 수 있으며, startDate는 가장 빠른 날짜, endDate는 가장 늦은 날짜를 반환합니다.
                    - 선예매와 일반 예매 정보는 각각 최대 1개만 존재하며, 해당 정보가 없으면 관련 필드가 모두 null로 반환됩니다.
    
                    ### 유의사항
                    - concertId가 유효하지 않으면 CONCERT_NOT_FOUND 에러가 발생합니다.
                    - 선예매 정보가 존재하면 일반 예매 정보도 반드시 존재합니다. 일반 예매만 단독으로 존재할 수 있습니다.
                    """
    )
    ResponseEntity<ConcertInfoResponse> getConcertInfo(
            CustomOAuth2User customOAuth2User,
            UUID concertId);
}
