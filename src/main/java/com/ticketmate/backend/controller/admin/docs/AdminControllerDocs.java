package com.ticketmate.backend.controller.admin.docs;

import com.ticketmate.backend.object.dto.admin.request.PortfolioSearchRequest;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomUserDetails;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface AdminControllerDocs {

    @Operation(
            summary = "공연장 정보 저장",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                    ### 요청 파라미터
                    - **concertHallName** (String): 공연장 명 (중복 불가) [필수]
                    - **capacity** (Integer): 수용인원 [필수]
                    - **address** (String): 공연장 주소 [필수]
                    - **concertHallUrl** (String): 공연장 웹사이트 URL [필수]
                                
                    ### 유의사항
                    - `concertHallName`은 고유해야 합니다.
                    - `concertHallUrl`은 'http://' 또는 'https://' 로 시작하는 문자열이어야 합니다

                    """
    )
    ResponseEntity<Void> saveHallInfo(
            CustomUserDetails customUserDetails,
            ConcertHallInfoRequest request);

    @Operation(
            summary = "공연 정보 저장",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                    ### 요청 파라미터
                    - **concertName** (String): 공연 제목 (중복 불가) [필수]
                    - **concertHallName** (String): 공연장 명 [필수]
                    - **concertType** (Enum): 공연 카테고리 [필수]
                    - **ticketPreOpenDate** (LocalDateTime): 선구매 오픈일 [선택]
                    - **ticketOpenDate** (LocalDateTime): 티켓 구매 오픈일 [필수]
                    - **duration** (Integer): 공연 시간 (분 단위) [필수]
                    - **session** (Integer): 공연 회차 [필수]
                    - **concertThumbNailUrl** (String): 콘서트 썸네일 URL [필수]
                    - **ticketReservationSite** (enum): 예매 사이트 [필수]
                                        
                    ### TicketReservationSite
                    INTERPARK_TICKET ("인터파크 티켓")
                                    
                    YES24_TICKET ("예스24 티켓")
                                    
                    TICKET_LINK ("티켓 링크")
                                    
                    MELON_TICKET ("멜론 티켓")
                                        
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
                    - 선예매 오픈일, 티켓 오픈일은 LocalDateTime으로 "yyyy-MM-dd'T'HH:mm:ss" 형식으로 입력해야합니다

                    """
    )
    ResponseEntity<Void> saveConcertInfo(
            CustomUserDetails customUserDetails,
            ConcertInfoRequest request);

    @Operation(
            summary = "의뢰인 -> 대리자로 바꾸기 위한 포트폴리오 리스트 조회",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                                
                    ### 유의사항
                    - 페이지당 10개의 포트폴리오 리스트 데이터를 반환합니다.
                    """
    )
    ResponseEntity<Page<PortfolioListForAdminResponse>> getPortfolioList(
            CustomUserDetails customUserDetails,
            PortfolioSearchRequest request);


    @Operation(
            summary = "의뢰인 -> 대리자로 바꾸기 위한 포트폴리오 상세 조회",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다
                    
                    ### 요청 파라미터
                    - 포트폴리오의 고유한 id
                                
                    ### 유의사항
                    - 포트폴리오의 id를 활용해 포트폴리오 상세조회시 관라지에게 필요한 데이터를 반환합니다. 
                    """
    )
    ResponseEntity<PortfolioForAdminResponse> getPortfolioInfo(
            CustomUserDetails customUserDetails, UUID id);
}
