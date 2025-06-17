package com.ticketmate.backend.controller.auth;

import com.ticketmate.backend.controller.auth.docs.AuthControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.service.member.MemberService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(
    name = "인증 관련 API",
    description = "회원 인증 관련 API 제공"
)
public class AuthController implements AuthControllerDocs {

  private final MemberService memberService;

  @Override
  @PostMapping(value = "/api/auth/reissue")
  @LogMonitoringInvocation
  public ResponseEntity<Void> reissue(HttpServletRequest request,
      HttpServletResponse response,
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    memberService.reissue(request, response);
    return ResponseEntity.ok().build();
  }
}
