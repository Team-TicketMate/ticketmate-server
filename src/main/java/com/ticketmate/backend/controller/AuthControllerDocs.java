package com.ticketmate.backend.controller;


import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface AuthControllerDocs {

    @Operation(
            summary = "회원가입",
            description = """
                                        
                    이 API는 인증이 필요하지 않습니다.

                    ### 요청 파라미터
                    - **username** (String): 사용자 이메일 (중복 불가)
                    - **password** (String): 사용자 비밀번호
                    - **nickname** (String): 사용자 닉네임 (중복 불가)
                    - **birth** (String): 생년월일 (형식: YYYYMMDD, 예: 19980114)
                    - **phone** (String): 전화번호 (형식: 01012345678, 11자리 숫자)
                    - **profileUrl** (String, 선택): 프로필 이미지 URL
                                
                    ### 유의사항
                    - `username`과 `nickname`은 고유해야 합니다.
                    - `birth`는 8자리의 문자열(예: YYYYMMDD) 형식을 따라야 합니다.
                    - `phone` 번호는 11자리 숫자 문자열이어야 합니다 (예: 01012345678).

                    """
    )
    ResponseEntity<ApiResponse<Void>> signUp(SignUpRequest request);
}
