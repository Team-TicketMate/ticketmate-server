package com.ticketmate.backend.domain.auth.controller;


import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.auth.domain.dto.request.SendCodeRequest;
import com.ticketmate.backend.domain.auth.domain.dto.request.VerifyCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthControllerDocs {

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
          - 이 API는 리프레시 토큰의 유효성을 검증한 후 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
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
  ResponseEntity<Void> reissue(
      HttpServletRequest request,
      HttpServletResponse response,
      CustomOAuth2User customOAuth2User);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-17",
          author = "Chuseok22",
          description = "본인인증 6자리 인증문자 발송 api",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/423"
      )
  })
  @Operation(
      summary = "본인인증 6자리 인증문자 발송",
      description = """
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - **phoneNumber** (String): 인증문자를 발송할 전화번호 [필수]
          
          ### 반환값
          **없음**
          
          ### 유의사항
          - 전화번호 형태는 서버측에서 자동으로 "01012345678" 형태로 정규화합니다
          """
  )
  ResponseEntity<Void> sendVerificationCode(
      SendCodeRequest request
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-07-17",
          author = "Chuseok22",
          description = "본인인증 6자리 인증문자 검증 api",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/423"
      )
  })
  @Operation(
      summary = "본인인증 6자리 인증문자 검증",
      description = """
          이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - **phoneNumber** (String): 인증문자를 발송한 전화번호 [필수]
          - **code** (String): 인증번호 6자리 [필수]
          
          ### 반환값
          **없음**
          
          ### 유의사항
          - 전화번호 형태는 서버측에서 자동으로 "01012345678" 형태로 정규화합니다
          - 인증번호는 숫자 6자리만 요청가능합니다
          """
  )
  ResponseEntity<Void> verifyVerificationCode(
      VerifyCodeRequest request
  );
}
