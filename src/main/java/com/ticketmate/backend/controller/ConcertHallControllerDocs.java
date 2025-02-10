package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ConcertHallControllerDocs {

    @Operation(
            summary = "공연장 정보 저장",
            description = """
                                        
                    이 API는 관리자 인증이 필요합니다

                    ### 요청 파라미터
                    - **concertHallName** (String): 공연장 명 (중복 불가)
                    - **capacity** (Integer): 수용인원
                    - **address** (String): 공연장 주소
                    - **concertHallUrl** (String): 공연장 웹사이트 URL
                                
                    ### 유의사항
                    - `concertHallName`은 고유해야 합니다.
                    - `concertHallUrl`은 'http://' 또는 'https://' 로 시작하는 문자열이어야 합니다

                    """
    )
    ResponseEntity<ApiResponse<Void>> saveHallInfo(
            CustomUserDetails customUserDetails,
            ConcertHallInfoRequest request);

    @Operation(
            summary = "공연장 정보 필터링",
            description = """
                                        
                    이 API는 인증이 필요합니다.

                    ### 요청 파라미터
                    - **concertHallName** (String): 공연장 이름 검색어 [선택]
                    - **minCapacity** (Integer): 최소 수용인원 필터링 [선택]
                    - **maxCapacity** (Integer): 최대 수용인원 필터링 [선택]
                    - **city** (String): 지역 [선택]
                    - **pageNumber** (Integer): 요청 페이지 번호 [선택]
                    - **pageSize** (Integer): 한 페이지 당 항목 수 [선택]
                    - **sortField** (String): 정렬할 필드 [선택]
                    - **sortDirection** (String): 정렬 방향 [선택]
                    
                    ### 사용 방법
                    `필터링 파라미터`
                    - concertHallName: 검색어가 포함된 공연장을 반환합니다
                    - maxCapacity, minCapacity: 수용 인원 (범위)
                    - city: 도시
                    
                    `정렬 조건`
                    - sortField: created_date(기본값), capacity
                    - sortDirection: ASC, DESC(기본값)
                    
                    `City`
                    SEOUL
                    INCHEON
                    GYEONGGI
                    DAEJEON
                    GANGWON
                    BUSAN
                    JEJU
                    DAEGU
                    GWANGJU
                    ULSAN
                    SEJONG
                    CHUNGCHEONGNAM
                    CHUNGCHEONGBUK
                    JEOLLANAM
                    JEOLLABUK
                    GYEONGSANGNAM
                    GYEONGSANGBUK
                                
                    ### 유의사항
                    - concertHallName, maxCapacity, minCapacity, city는 요청하지 않을 경우 필터링 조건에 적용되지 않습니다
                    - sortField, sortType은 해당하는 문자열만 입력 가능합니다.
                    """
    )
    ResponseEntity<ApiResponse<Page<ConcertHallFilteredResponse>>> filteredConcertHall(
            CustomUserDetails customUserDetails,
            ConcertHallFilteredRequest request);
}
