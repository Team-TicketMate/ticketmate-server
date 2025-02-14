package com.ticketmate.backend.controller;


import com.ticketmate.backend.object.dto.SignInRequest;
import com.ticketmate.backend.object.dto.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    ResponseEntity<Void> signUp(SignUpRequest request);

    @Operation(
            summary = "로그인",
            description = """
                                        
                    이 API는 인증이 필요하지 않습니다.

                    ### 요청 파라미터
                    - **username** (String): 사용자 이메일
                    - **password** (String): 사용자 비밀번호
                                
                    ### 유의사항
                    - 개발자의 편의를 위해 만들어진 API 입니다.

                    """
    )
    ResponseEntity<Void> signIn(SignInRequest request);

    @Operation(
            summary = "accessToken 재발급 요청",
            description = """
                                        
                    이 API는 인증이 필요하지 않습니다.
                    RefreshToken만으로 접근 가능합니다.
                    클라이언트는 HTTP-Only 쿠키로 저장된 RefreshToken을 이용하여 새로운 AccessToken을 발급받을 수 있습니다.

                    ### 요청 파라미터
                    - **Cookie**: RefreshToken이 포함된 HTTP-Only 쿠키
                        - **Name**: `refreshToken`
                        - **Value**: `저장된 리프레시 토큰 값`
                        
                    ### 반환값
                    **없음**
                                
                    ### 유의사항
                    - 이 API는 리프레시 토큰의 유효성을 검증한 후 새로운 액세스 토큰을 발급합니다.
                    - 리프레시 토큰은 쿠키로 저장되며, 클라이언트에서 직접 접근할 수 없으므로, 쿠키는 자동으로 서버로 전송됩니다.
                    - 새로운 액세스 토큰은 반환된 후, 클라이언트는 이를 사용하여 인증이 필요한 API 요청에 사용할 수 있습니다.
                    - 리프레시 토큰이 만료되었거나 유효하지 않을 경우, 서버에서 401 Unauthorized 상태 코드가 반환되며, 클라이언트는 사용자를 다시 로그인시켜야 합니다.
                      
                    **응답 코드:**
                                
                    - **200 OK**: 새로운 액세스 토큰 발급 성공
                    - **401 Unauthorized**: 리프레시 토큰이 유효하지 않거나 만료됨
                    - **400 Bad Request**: 쿠키에서 리프레시 토큰을 찾을 수 없음
                                
                    **추가 설명:**
                                
                    - 리프레시 토큰을 이용한 액세스 토큰 재발급은 보안을 강화하는 방법으로, 클라이언트가 리프레시 토큰을 저장할 필요가 없습니다.
                    - 리프레시 토큰은 자동으로 쿠키로 전송되며, 쿠키는 HTTP-Only 속성으로 설정되어 있기 때문에 클라이언트에서 접근할 수 없습니다.
                    """
    )
    ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response);
}
