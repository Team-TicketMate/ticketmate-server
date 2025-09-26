package com.ticketmate.backend.api.application.controller.auth;

import com.ticketmate.backend.auth.application.dto.request.LoginRequest;
import com.ticketmate.backend.auth.application.dto.response.LoginResponse;
import com.ticketmate.backend.auth.application.service.AuthService;
import com.ticketmate.backend.auth.application.service.SmsAuthService;
import com.ticketmate.backend.auth.application.service.TotpAuthService;
import com.ticketmate.backend.auth.infrastructure.constant.AuthConstants;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.sms.application.dto.SendCodeRequest;
import com.ticketmate.backend.sms.application.dto.VerifyCodeRequest;
import com.ticketmate.backend.totp.application.dto.request.TotpVerifyRequest;
import com.ticketmate.backend.totp.application.dto.response.TotpSetupResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(
    name = "인증 관련 API",
    description = "회원 인증 관련 API 제공"
)
public class AuthController implements AuthControllerDocs {

  private final AuthService authService;
  private final TotpAuthService totpAuthService;
  private final SmsAuthService smsAuthService;

  @Override
  @PostMapping(value = "/login")
  @LogMonitoringInvocation
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Override
  @PostMapping(value = "/reissue")
  @LogMonitoringInvocation
  public ResponseEntity<Void> reissue(
      HttpServletRequest request,
      HttpServletResponse response) {
    authService.reissue(request, response);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping(value = "/2fa/setup")
  @LogMonitoringInvocation
  public ResponseEntity<TotpSetupResponse> totpSetup(
      @RequestHeader(AuthConstants.HEADER_PRE_AUTH) String preAuthToken) {
    return ResponseEntity.ok(totpAuthService.setupTotp(preAuthToken));
  }

  @Override
  @PostMapping(value = "/2fa/setup/verify")
  @LogMonitoringInvocation
  public ResponseEntity<Void> verifySetupTotp(
      @RequestHeader(AuthConstants.HEADER_PRE_AUTH) String preAuthToken,
      @Valid @RequestBody TotpVerifyRequest request) {
    totpAuthService.verifySetupTotp(preAuthToken, request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(value = "/2fa/login/verify")
  @LogMonitoringInvocation
  public ResponseEntity<Boolean> verifyLoginTotp(
      @RequestHeader(AuthConstants.HEADER_PRE_AUTH) String preAuthToken,
      @Valid @RequestBody TotpVerifyRequest request,
      HttpServletResponse response) {
    return ResponseEntity.ok(totpAuthService.verifyLoginTotp(preAuthToken, request, response));
  }

  @Override
  @PostMapping(value = "/2fa/reset")
  @LogMonitoringInvocation
  public ResponseEntity<Void> resetTotp(
      @RequestHeader(AuthConstants.HEADER_PRE_AUTH) String preAuthToken) {
    totpAuthService.resetTotp(preAuthToken);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(value = "/sms/send-code")
  @LogMonitoringInvocation
  public ResponseEntity<Void> sendVerificationCode(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody SendCodeRequest request) {
    smsAuthService.sendVerificationCode(request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(value = "/sms/verify")
  @LogMonitoringInvocation
  public ResponseEntity<Void> verifyVerificationCode(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody VerifyCodeRequest request
  ) {
    smsAuthService.verifyVerificationCode(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }
}
