package com.ticketmate.backend.controller.application;

import com.ticketmate.backend.controller.application.docs.ApplicationFormControllerDocs;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.object.dto.application.request.ApplicationFormRequest;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.expressions.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.service.application.ApplicationFormService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/application")
@Tag(
        name = "신청서 관련 API",
        description = "대리 티켓팅 신청서 관련 API 제공"
)
public class ApplicationFormController implements ApplicationFormControllerDocs {

    private final ApplicationFormService applicationFormService;

    @Override
    @PostMapping("")
    @LogMonitoringInvocation
    public ResponseEntity<Void> saveApplicationForm(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @RequestBody ApplicationFormRequest request) {
        applicationFormService.createApplicationForm(request, customOAuth2User.getMember());
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("")
    @LogMonitoringInvocation
    public ResponseEntity<Page<ApplicationFormFilteredResponse>> filteredApplicationForm(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @ModelAttribute ApplicationFormFilteredRequest request) {
        return ResponseEntity.ok(applicationFormService.filteredApplicationForm(request));
    }

    @Override
    @GetMapping("/{applicationFormId}")
    @LogMonitoringInvocation
    public ResponseEntity<ApplicationFormFilteredResponse> applicationFormInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable UUID applicationFormId) {
        return ResponseEntity.ok(applicationFormService.getApplicationFormInfo(applicationFormId));
    }

    @Override
    @PutMapping("/expressions/{application-form-id}/reject")
    @LogMonitoringInvocation
    public void reject(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                       @PathVariable(value = "application-form-id") UUID applicationFormId,
                       @RequestBody ApplicationFormRejectRequest request) {
        Member member = customOAuth2User.getMember();
        applicationFormService.reject(applicationFormId, member, request);
    }

    @Override
    @PutMapping("/expressions/{application-form-id}/approve")
    @LogMonitoringInvocation
    public ResponseEntity<String> approve(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                       @PathVariable(value = "application-form-id") UUID applicationFormId) {
        Member member = customOAuth2User.getMember();
        return ResponseEntity.ok(applicationFormService.approve(applicationFormId, member));
    }

    @Override
    @PostMapping("/duplicate")
    @LogMonitoringInvocation
    public ResponseEntity<Boolean> isDuplicateApplicationForm(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @RequestBody ApplicationFormDuplicateRequest request) {
        Member client = customOAuth2User.getMember();
        return ResponseEntity.ok(applicationFormService.isDuplicateApplicationForm(client, request));
    }
}
