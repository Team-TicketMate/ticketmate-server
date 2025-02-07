package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.ConcertHallInfoRequest;
import com.ticketmate.backend.object.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
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
}
