package com.ticketmate.backend.controller.fcm;

import com.ticketmate.backend.controller.fcm.docs.FcmControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.fcm.request.FcmTokenSaveRequest;
import com.ticketmate.backend.object.dto.fcm.response.FcmTokenSaveResponse;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.service.fcm.FcmService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
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
        return ResponseEntity.ok(fcmService.saveFcmToken(request,member));
    }
}
