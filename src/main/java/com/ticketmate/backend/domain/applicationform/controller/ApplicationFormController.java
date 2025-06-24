package com.ticketmate.backend.domain.applicationform.controller;

import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.service.ApplicationFormService;
import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @GetMapping("/{application-form-id}")
  @LogMonitoringInvocation
  public ResponseEntity<ApplicationFormFilteredResponse> applicationFormInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    return ResponseEntity.ok(applicationFormService.getApplicationFormInfo(applicationFormId));
  }

  @Override
  @PutMapping("/expressions/{application-form-id}/reject")
  @LogMonitoringInvocation
  public void reject(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId,
      @RequestBody ApplicationFormRejectRequest request) {
    Member member = customOAuth2User.getMember();
    applicationFormService.rejectApplicationForm(applicationFormId, member, request);
  }

  @Override
  @PutMapping("/expressions/{application-form-id}/approve")
  @LogMonitoringInvocation
  public ResponseEntity<String> approve(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    Member member = customOAuth2User.getMember();
    return ResponseEntity.ok(applicationFormService.acceptedApplicationForm(applicationFormId, member));
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
