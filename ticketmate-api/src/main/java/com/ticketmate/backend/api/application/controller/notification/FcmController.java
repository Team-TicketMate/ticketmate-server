package com.ticketmate.backend.api.application.controller.notification;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.notification.application.dto.request.FcmTokenSaveRequest;
import com.ticketmate.backend.notification.application.dto.response.FcmTokenSaveResponse;
import com.ticketmate.backend.notification.infrastructure.service.FcmTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
@Tag(
    name = "FCM 관련 API",
    description = "FCM 관련 API 제공"
)
public class FcmController implements FcmControllerDocs {

  private final FcmTokenService fcmTokenService;

  @Override
  @PostMapping(value = "")
  @LogMonitoringInvocation
  public ResponseEntity<FcmTokenSaveResponse> saveFcmToken(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestBody @Valid FcmTokenSaveRequest request) {
    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(fcmTokenService.saveFcmToken(request, member.getMemberId()));
  }
}
