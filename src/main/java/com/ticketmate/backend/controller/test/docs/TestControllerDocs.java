package com.ticketmate.backend.controller.test.docs;

import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface TestControllerDocs {

    @Operation(
            summary = "테스트 로그인",
            description = """
                    
                    이 API는 인증이 필요하지 않습니다.

                    ### 요청 파라미터
                    - **socialPlatform** (String): 소셜 플랫폼 [필수]
                    - **memberType** (String): 의뢰인/대리자 (기본: 의뢰인)
                    - **accountStatus** (String): 활성화/삭제 (기본: 활성화)
                    - **isFirstLogin** (Boolean): 첫 로그인 여부

                    ### 유의사항
                    - 개발자의 편의를 위한 소셜 로그인 회원가입/로그인 메서드입니다
                    - 스웨거에서 테스트 용도로만 사용해야하며, 엑세스 토큰만 제공됩니다.

                    """
    )
    ResponseEntity<String> socialLogin(LoginRequest request);

    @Operation(
            summary = "테스트 회원 삭제",
            description = """
                    
                    이 API는 인증이 필요하지 않습니다.

                    ### 요청 파라미터
                    `없음`

                    ### 유의사항
                    - 데이터베이스에 저장되어있는 모든 테스트 유저를 삭제합니다.

                    """
    )
    ResponseEntity<Void> deleteTestMember();
}
