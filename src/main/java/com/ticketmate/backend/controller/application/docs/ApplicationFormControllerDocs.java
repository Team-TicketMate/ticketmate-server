package com.ticketmate.backend.controller.application.docs;

import com.ticketmate.backend.object.dto.application.request.ApplicationFormRequest;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface ApplicationFormControllerDocs {

    @Operation(
            summary = "대리 티켓팅 신청서 작성",
            description = """
                                        
                    이 API는 인증이 필요합니다

                    ### 요청 파라미터
                    - **agentId** (UUID): 대리인 PK [필수]
                    - **concertId** (UUID): 콘서트 PK [필수]
                    - **requestCount** (Integer): 티켓 요청 매수 [필수] (default: 1)
                    - **hopeAreas** (Map<String, String>): 희망구역
                    - **requestDetails** (String): 요청사항
                                                    
                    ### 유의사항
                    - 티켓 요청 매수는 1 이상의 정수를 입력해주세요
                    - 희망구역은 최대 10개까지 등록 가능합니다
                    """
    )
    ResponseEntity<Void> saveApplicationForm(
            CustomOAuth2User customOAuth2User,
            ApplicationFormRequest request);
}
