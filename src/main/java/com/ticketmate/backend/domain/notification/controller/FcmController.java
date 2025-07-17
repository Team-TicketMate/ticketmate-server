package com.ticketmate.backend.domain.notification.controller;

import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.notification.domain.dto.request.FcmTokenSaveRequest;
import com.ticketmate.backend.domain.notification.domain.dto.response.FcmTokenSaveResponse;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.service.FcmService;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
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

  private final FcmService fcmService;

  @Override
  @PostMapping(value = "")
  @LogMonitoringInvocation
  public ResponseEntity<FcmTokenSaveResponse> saveFcmToken(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestBody @Valid FcmTokenSaveRequest request) {
    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(fcmService.saveFcmToken(request, member));
  }
}
