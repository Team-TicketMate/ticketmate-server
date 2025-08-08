package com.ticketmate.backend.api.application.controller.auth;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.ticketmate.backend.auth.application.dto.request.LoginRequest;
import com.ticketmate.backend.auth.application.dto.response.LoginResponse;
import com.ticketmate.backend.sms.application.dto.SendCodeRequest;
import com.ticketmate.backend.sms.application.dto.VerifyCodeRequest;
import com.ticketmate.backend.totp.application.dto.request.TotpVerifyRequest;
import com.ticketmate.backend.totp.application.dto.response.TotpSetupResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthControllerDocs {

  @Operation(
      summary = "관리자 로그인",
      description = """
          ### 이 API는 인증이 필요하지 않습니다.
          
          ### 요청 파라미터
          - `username` (String, required): 로그인 아이디  
          - `password` (String, required): 로그인 비밀번호  
          
          ### 응답 데이터
          ```json
          {
            "totpEnabled": boolean,
            "preAuthToken": string
          }
          ```
          - `totpEnabled`: 2차인증 설정 여부 (true면 2차인증 화면으로, false면 2차인증 설정 화면으로 이동)
          - `preAuthToken`: 후속 2차 인증 API 호출 시 반드시 헤더에 담아야 하는 토큰
          
          
          ### 사용 예시
          1. `POST /api/auth/login` 호출
             ```json
             { "username": "user", "password": "pass" }
             ```
          2. 응답에서 `totpEnabled`, `preAuthToken` 값을 확인
             - `totpEnabled = false` → `2차인증 설정화면` 으로 이동
             - `totpEnabled = true`  → `2차인증 인증화면` 으로 이동
          3. 발급된 `preAuthToken`은 "X-PreAuth-Token" HTTP 헤더로 등록하여 추후 2차인증 임시 토큰으로 활용됩니다
          
          ### 유의 사항
          - 모든 요청 필드는 반드시 값이 존재해야 합니다.  
          """
  )
  ResponseEntity<LoginResponse> login(
      LoginRequest request
  );

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
      HttpServletResponse response);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-06",
          author = "Chuseok22",
          description = "TOTP setup API",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/258"
      ),
      @ApiChangeLog(
          date = "2025-08-05",
          author = "Chuseok22",
          description = "Google Authenticator 2차인증 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/258"
      )
  })
  @Operation(
      summary = "TOTP 설정 (OTP 앱 등록 URL 발급)",
      description = """
          ### 인증 헤더
          - `X-PreAuth-Token` (String, required): `/login` 호출 시 받은 preAuthToken
          
          ### 요청 파라미터
          `없음`
          
          ### 응답 데이터
          - `secret` (String): 백업용 시크릿 키
          - `otpAuthUrl` (String): OTP 앱(Google Authenticator 등) 등록용 URI
          
          ### 사용 방법
          1. 로그인 후 받은 `preAuthToken`을 `X-PreAuth-Token` 헤더에 담아 호출 
          2. 응답으로 받은 `otpAuthUrl`을 QR 코드로 렌더링 
          3. Google Authenticator 앱에서 QR 스캔 후 등록 완료
          
          ### 유의 사항
          - 이미 TOTP가 설정된 사용자가 재호출할 경우, 기존 시크릿이 재발급되며 이전에 등록된 앱에서는 더 이상 코드가 유효하지 않습니다.
          - 이미 등록된 사용자는 호출 시 에러 발생  
          - QR 코드 스캔 전에 URL을 외부에 노출하지 않도록 주의하세요.
          - `otpAuthUrl`은 발급 후 5분 이내에 사용해야 합니다
          """
  )
  ResponseEntity<TotpSetupResponse> totpSetup(
      String preAuthToken
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-06",
          author = "Chuseok22",
          description = "TOTP setup verify API",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/258"
      ),
      @ApiChangeLog(
          date = "2025-08-05",
          author = "Chuseok22",
          description = "Google Authenticator 2차인증 개발",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/258"
      )
  })
  @Operation(
      summary = "TOTP 초기 등록 검증",
      description = """
          ### 인증 헤더
          - `X-PreAuth-Token` (String, required): `/login` 호출 시 받은 preAuthToken
          
          ### 요청 파라미터
          - `code` (String, required): 사용자가 OTP 앱에서 입력한 6자리 TOTP 코드
          
          #### Request Body 예시
          ```json
          {
            "code": 123456
          }
          ```
          
          ### 응답 데이터
          - `없음` 200OK
          
          ### 사용 방법
          1. QR 스캔 후 앱에 표시된 코드를 입력
          2. `X-PreAuth-Token` 헤더와 코드 본문으로 `POST /api/auth/2fa/setup/verify` 호출 
          
          ### 유의 사항
          - 코드 입력 후 30초(타임 윈도우) 경과 시 자동으로 만료됩니다.  
          """
  )
  ResponseEntity<Void> verifySetupTotp(
      String preAuthToken,
      TotpVerifyRequest request
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-06",
          author = "Chuseok22",
          description = "로그인 2차인증 검증 API",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/258"
      )
  })
  @Operation(
      summary = "관리자 로그인 2차인증 검증 API",
      description = """
          ### 인증 헤더
          - `X-PreAuth-Token` (String, required): `/login` 호출 시 받은 preAuthToken
          
          ### 요청 파라미터
          - `code` (String, required): 사용자가 OTP 앱에서 입력한 6자리 TOTP 코드
          
          #### Request Body 예시
          ```json
          {
            "code": 123456
          }
          ```
          
          ### 응답 데이터
          - HTTP 200 OK: `true` (boolean)
          - 쿠키: `ACCESS_TOKEN`, `REFRESH_TOKEN` (JWT) 자동 설정
          - 2차인증에 성공하면 `true` 를 반환하며 엑세스토큰, 리프레시토큰이 포함된 쿠키를 반환합니다.
          
          ### 사용 방법
          1. `X-PreAuth-Token` 헤더와 코드 본문으로 `POST /api/auth/2fa/login/verify` 호출
          2. 서버에서 코드 검증 및 JWT 발급 후 `true` 반환
          
          ### 유의 사항
          - 2FA 활성 상태가 아니면 400에러를 반환합니다
          - 코드 입력 후 30초(타임 윈도우) 경과 시 자동으로 만료됩니다.  
          """
  )
  ResponseEntity<Boolean> verifyLoginTotp(
      String preAuthToken,
      TotpVerifyRequest request,
      HttpServletResponse response
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-08-06",
          author = "Chuseok22",
          description = "2차인증 초기화",
          issueUrl = "https://github.com/Team-TicketMate/ticketmate-server/issues/258"
      )
  })
  @Operation(
      summary = "TOTP 2차인증 초기화",
      description = """
          ### 인증 헤더
          - `X-PreAuth-Token` (String, required): `/login` 호출 시 받은 preAuthToken
          
          ### 요청 파라미터
          - `없음`
          
          ### 응답 데이터
          - `없음`
          
          ### 사용 예시
          1. `X-PreAuth-Token` 헤더로 `POST /api/auth/2fa/reset` 호출
          2. 서버에서 2FA 설정 초기화 후 200 OK 반환
          3. 이후 다시 로그인 시 `/api/auth/login` → `/api/auth/2fa/setup` 절차 필요
          
          ### 유의 사항
          - 2FA 활성 상태가 아니면 400에러를 반환합니다
          - 기존에 등록된 2FA관련 데이터를 삭제합니다
          - 이후 해당 계정으로 로그인 시, 2FA setup을 다시 설정해야합니다
          """
  )
  ResponseEntity<Void> resetTotp(
      String preAuthToken
  );

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
