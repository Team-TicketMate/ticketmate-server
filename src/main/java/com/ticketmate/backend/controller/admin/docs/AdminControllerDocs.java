package com.ticketmate.backend.controller.admin.docs;

import com.ticketmate.backend.object.dto.admin.request.PortfolioSearchRequest;
import com.ticketmate.backend.object.dto.admin.request.PortfolioStatusUpdateRequest;
import com.ticketmate.backend.object.dto.admin.response.ConcertHallFilteredAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface AdminControllerDocs {

    /*
    ======================================공연장======================================
     */

    @Operation(
            summary = "공연장 정보 저장",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                    ### 요청 파라미터
                    - **concertHallName** (String): 공연장 명 (중복 불가) [필수]
                    - **address** (String): 공연장 주소 [선택]
                    - **webSiteUrl** (String): 공연장 웹사이트 URL [선택]
                                
                    ### 유의사항
                    - `concertHallName`은 고유해야 합니다.
                    - `webSiteUrl`은 'http://' 또는 'https://' 로 시작하는 문자열이어야 합니다
                    - 공연장 등록이 정상적으로 진행된 경우 `201 CREATED` 를 반환합니다
                    """
    )
    ResponseEntity<Void> saveHallInfo(
            CustomOAuth2User customOAuth2User,
            ConcertHallInfoRequest request);

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
                    - sortField: created_date(기본값)
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
                    - sortField, sortType은 해당하는 문자열만 입력 가능합니다.
                    """
    )
    ResponseEntity<Page<ConcertHallFilteredAdminResponse>> filteredConcertHall(
            CustomOAuth2User customOAuth2User,
            ConcertHallFilteredRequest request);

    /*
    ======================================공연======================================
     */

    @Operation(
            summary = "공연 정보 저장",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                    ### 요청 파라미터
                    - **concertName** (String): 공연 제목 (중복 불가) [필수]
                    - **concertHallId** (UUID): 공연장 PK [선택]
                    - **concertType** (Enum): 공연 카테고리 [필수]
                    - **concertThumbNail** (MultipartFile): 콘서트 썸네일 파일 [필수]
                    - **seatingChart** (MultipartFile): 좌석 배치도 파일 [선택]
                    - **ticketReservationSite** (enum): 예매 사이트 [선택]
                    - **concertDateRequests** (List\\<ConcertDateRequest\\>): 공연 날짜 DTO [선택]
                    - **ticketOpenDateRequests** (List\\<TicketOpenDateRequest\\>): 티켓 오픈일 DTO [선택]
                                        
                    ### ConcertDateRequest
                    - **concertDate** (LocalDateTime): 공연 일자 [필수]
                    - **session** (Integer): 공연 회차 [필수]
                    
                    ### TicketOpenDateRequest
                    - **ticketOpenDate** (LocalDateTime): 티켓 오픈일 [선택]
                    - **requestMaxCount** (Integer): 최대 예매 개수 [선택]
                    - **isBankTransfer** (Boolean): 무통장 입금 여부 [선택]
                    - **isPreOpen** (Boolean): 선예매, 일반예매 여부 [필수]
                    
                    ### TicketReservationSite
                    INTERPARK_TICKET ("인터파크 티켓")
                                    
                    YES24_TICKET ("예스24 티켓")
                                    
                    TICKET_LINK ("티켓 링크")
                                    
                    MELON_TICKET ("멜론 티켓")
                    
                    COUPANG_PLAY ("쿠팡 플레이")
                    
                    ETC ("기타")
                                        
                    ### ConcertType
                    CONCERT ("콘서트")
                                    
                    MUSICAL ("뮤지컬")
                                    
                    SPORTS ("스포츠")
                                    
                    CLASSIC ("클래식")
                                    
                    EXHIBITIONS ("전시")
                                    
                    OPERA ("오페라")

                    ETC ("기타")
                                
                    ### 유의사항
                    - `concertName`은 고유해야 합니다.
                    - 단일 회차 공연의 경우 "1"을 입력하면 됩니다.
                    - 티켓 오픈일은 LocalDateTime으로 "yyyy-MM-dd'T'HH:mm:ss" 형식으로 입력해야합니다

                    """
    )
    ResponseEntity<Void> saveConcertInfo(
            CustomOAuth2User customOAuth2User,
            ConcertInfoRequest request);

    /*
    ======================================포트폴리오======================================
     */

    @Operation(
            summary = "의뢰인 -> 대리자로 바꾸기 위한 포트폴리오 리스트 조회",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                                
                    ### 유의사항
                    - 페이지당 10개의 포트폴리오 리스트 데이터를 반환합니다.
                    """
    )
    ResponseEntity<Page<PortfolioListForAdminResponse>> getPortfolioList(
            CustomOAuth2User customOAuth2User,
            PortfolioSearchRequest request);


    @Operation(
            summary = "의뢰인 -> 대리자로 바꾸기 위한 포트폴리오 상세 조회",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다
                    
                    ### 요청 파라미터
                    - 포트폴리오의 고유한 id                  
                                
                    ### 유의사항
                    - 포트폴리오의 id를 활용해 포트폴리오 상세조회시 관라지에게 필요한 데이터를 반환합니다.
                   
                    ### 알림전송 특이사항
                    - **UNDER_REVIEW** 상태의 포트폴리오를 **REVIEWING** 상태로 변경합니다.
                    - 변경하며 해당 포트폴리오를 올린 사용자에 대해서 알림을 발송합니다.
                    - 백엔드측 알림구현은 Web푸시 알림을 구현하여 전송합니다.
                    - 푸시알림시 1:N 플랫폼의 사용자를 대비하기 위해 기존에 만들어놓은 RedisHash스키마를 활용하여 사용자의 모든 플랫폼에 알림을 전송합니다.
                    """
    )
    ResponseEntity<PortfolioForAdminResponse> getPortfolioInfo(
            CustomOAuth2User customOAuth2User, UUID portfolioId);

    @Operation(
            summary = "요청한 포트폴리오 승인, 반려처리",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다
                    
                    ### 요청 파라미터
                    - 변경할 포트폴리오의 고유한 id
                    - 반려 및 승인의 Body 파라미터
                    
                    ### PortfolioType
                    
                    REVIEW_COMPLETED ("승인된 포트폴리오")
                    
                    COMPANION("반려된 포트폴리오")
                                
                    ### 유의사항
                    - 관리자가 승인 요청이된 포트폴리오를 승인 및 반려하는 작업입니다.
                    - 승인(REVIEW_COMPLETED)시 해당 포트폴리오의 상태가 "REVIEW_COMPLETED("승인된 포트폴리오")" 로 변경됩니다.
                    - 반려(COMPANION)시 해당 포트폴리오의 상태가 "COMPANION("반려된 포트폴리오")" 로 변경됩니다. 
                    
                    ### 알림전송 특이사항
                    - 관리자가 포트폴리오를 승인 혹은 반려 상태로 변경합니다.
                    - 상태가 변경되면 해당 포트폴리오를 올린 사용자에 대해서 알림을 발송합니다.
                    - 백엔드측 알림구현은 Web푸시 알림을 구현하여 전송합니다.
                    - 푸시알림시 1:N 플랫폼의 사용자를 대비하기 위해 기존에 만들어놓은 RedisHash스키마를 활용하여 사용자의 모든 플랫폼에 알림을 전송합니다.
                    """
    )
    ResponseEntity<UUID> reviewPortfolio(
            CustomOAuth2User customOAuth2User, UUID portfolioId, PortfolioStatusUpdateRequest request);
}
