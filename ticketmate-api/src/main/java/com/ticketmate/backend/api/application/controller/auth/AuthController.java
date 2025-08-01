package com.ticketmate.backend.api.application.controller.auth;

import com.ticketmate.backend.auth.application.service.AuthService;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.sms.application.dto.SendCodeRequest;
import com.ticketmate.backend.sms.application.dto.VerifyCodeRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping(value = "/send-code")
  @LogMonitoringInvocation
  public ResponseEntity<Void> sendVerificationCode(
      @Valid @RequestBody SendCodeRequest request) {
    authService.sendVerificationCode(request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(value = "/verify")
  @LogMonitoringInvocation
  public ResponseEntity<Void> verifyVerificationCode(
      @Valid @RequestBody VerifyCodeRequest request) {
    authService.verifyVerificationCode(request);
    return ResponseEntity.ok().build();
  }
}
