package com.ticketmate.backend.controller.test.docs;

import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public interface TestControllerDocs {

    @Operation(
            summary = "테스트 로그인",
            description = """
                    
                    이 API는 인증이 필요하지 않습니다.
                    
                    ### 요청 파라미터
                    - **role** (String): 회원 권한 [필수]
                    - **socialPlatform** (String): 소셜 플랫폼 [필수]
                    - **memberType** (String): 의뢰인/대리자 (기본: 의뢰인)
                    - **accountStatus** (String): 활성화/삭제 (기본: 활성화)
                    - **isFirstLogin** (Boolean): 첫 로그인 여부
                    
                    ### 유의사항
                    - 개발자의 편의를 위한 소셜 로그인 회원가입/로그인 메서드입니다
                    - 스웨거에서 테스트 용도로만 사용해야하며, 엑세스 토큰만 제공됩니다.
                    - `ROLE_TEST`, `ROLE_TEST_ADMIN`만 선택 가능합니다
                    - username을 입력하지 않을 시 임의의 사용자가 생성됩니다
                    """
    )
    ResponseEntity<String> socialLogin(LoginRequest request);

    @Operation(
            summary = "회원 Mock 데이터 생성",
            description = """
                    ### ✅ 요청 파라미터
                    - `count` (int): 생성할 Mock 회원 수 (1 이상 필수)
                    
                    ### 🔄 응답 데이터
                    - HTTP 200 OK
                    - 반환값: 생성된 회원 수 (Integer)
                    
                    ```json
                    {
                      "result": 10
                    }
                    ```
                    
                    ### 🛠️ 사용 방법
                    - 테스트 환경에서 회원 데이터가 필요할 때 사용하는 API입니다.
                    - 요청 시 `count`만큼의 Mock 회원 데이터를 생성하여 DB에 저장합니다.
                    - 내부적으로 멀티스레드로 처리되므로 빠른 속도로 대량 데이터를 생성할 수 있습니다.
                    
                    ### ⚠️ 유의 사항
                    - `count`가 1 미만일 경우 기본값 1로 처리됩니다.
                    - 생성된 회원 정보는 랜덤 데이터 기반이며 실제 유저 데이터가 아닙니다.
                    - 해당 API는 운영 환경에서 사용하지 않도록 주의해야 합니다.
                    
                    ### ❌ 예외 처리
                    - `INTERNAL_SERVER_ERROR (500)`: 회원 Mock 데이터 저장 중 예기치 못한 오류가 발생한 경우
                    - `INVALID_MEMBER_ROLE_REQUEST (400)`: 테스트 전용 Role(ROLE_TEST, ROLE_TEST_ADMIN) 외의 값이 요청될 경우
                    """
    )
    ResponseEntity<CompletableFuture<Integer>> generateMockMembers(int count);

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

    @Operation(
            summary = "테스트 공연장 데이터 추가",
            description = """
                    
                    이 API는 인증이 필요하지 않습니다.
                    
                    ### 요청 파라미터
                    - **count** (Integer): 추가할 Mock 데이터 개수 (선택)
                    
                    ### 유의사항
                    - 생성되는 공연장 Mock 데이터 기본값은 30개입니다
                    - 중복 데이터 생성에 따라 사용자가 원하는 개수보다 적은 데이터가 저장될 수 있습니다
                    
                    """
    )
    ResponseEntity<Void> createConcertHallMockData(Integer count);

    @Operation(
            summary = "테스트 공연 데이터 추가",
            description = """
                    
                    이 API는 인증이 필요하지 않습니다.
                    
                    ### 요청 파라미터
                    - **count** (Integer): 추가할 Mock 데이터 개수 (선택)
                    
                    ### 유의사항
                    - 생성되는 공연 Mock 데이터 기본값은 30개입니다
                    - 중복 데이터 생성에 따라 사용자가 원하는 개수보다 적은 데이터가 저장될 수 있습니다
                    
                    """
    )
    ResponseEntity<Void> createConcertMockData(Integer count);

    @Operation(
            summary = "테스트 신청서 데이터 추가",
            description = """
                    
                    이 API는 인증이 필요하지 않습니다.
                    
                    ### 요청 파라미터
                    - **count** (Integer): 추가할 Mock 데이터 개수 (선택)
                    
                    ### 유의사항
                    - 생성되는 신청서 Mock 데이터 기본값은 30개입니다
                    - 중복 데이터 생성에 따라 사용자가 원하는 개수보다 적은 데이터가 저장될 수 있습니다
                    """
    )
    ResponseEntity<Void> createApplicationFormMockData(Integer count);
}
