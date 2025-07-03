package com.ticketmate.backend.domain.applicationform.controller;

import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormEditRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.domain.applicationform.service.ApplicationFormService;
import com.ticketmate.backend.domain.applicationform.service.RejectionReasonService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/application-form")
@Tag(
    name = "신청서 관련 API",
    description = "대리 티켓팅 신청서 관련 API 제공"
)
public class ApplicationFormController implements ApplicationFormControllerDocs {

  private final ApplicationFormService applicationFormService;
  private final RejectionReasonService rejectionReasonService;

  @Override
  @PostMapping("")
  @LogMonitoringInvocation
  public ResponseEntity<Void> saveApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestBody @Valid ApplicationFormRequest request) {
    applicationFormService.createApplicationForm(request, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("")
  @LogMonitoringInvocation
  public ResponseEntity<Page<ApplicationFormFilteredResponse>> filteredApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ModelAttribute @Valid ApplicationFormFilteredRequest request) {
    return ResponseEntity.ok(applicationFormService.filteredApplicationForm(request));
  }

  @Override
  @GetMapping("/{application-form-id}")
  @LogMonitoringInvocation
  public ResponseEntity<ApplicationFormFilteredResponse> getApplicationFormInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    return ResponseEntity.ok(applicationFormService.getApplicationFormInfo(applicationFormId));
  }

  @Override
  @PatchMapping("/{application-form-id}/edit")
  @LogMonitoringInvocation
  public ResponseEntity<Void> editApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId,
      @RequestBody @Valid ApplicationFormEditRequest applicationFormEditRequest) {
    applicationFormService.editApplicationForm(applicationFormId, applicationFormEditRequest, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping("/{application-form-id}/cancel")
  @LogMonitoringInvocation
  public ResponseEntity<Void> cancelApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    applicationFormService.cancelApplicationForm(applicationFormId, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping("/{application-form-id}/reject")
  @LogMonitoringInvocation
  public ResponseEntity<Void> rejectApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId,
      @RequestBody @Valid ApplicationFormRejectRequest request) {
    applicationFormService.rejectApplicationForm(applicationFormId, customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping("/{application-form-id}/accept")
  @LogMonitoringInvocation
  public ResponseEntity<String> approve(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    return ResponseEntity.ok(applicationFormService.acceptedApplicationForm(applicationFormId, customOAuth2User.getMember()));
  }

  @Override
  @PostMapping("/duplicate")
  @LogMonitoringInvocation
  public ResponseEntity<Boolean> isDuplicateApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestBody @Valid ApplicationFormDuplicateRequest request) {
    Member client = customOAuth2User.getMember();
    return ResponseEntity.ok(applicationFormService.isDuplicateApplicationForm(client, request));
  }

  @Override
  @GetMapping("/{application-form-id}/rejection-reason")
  @LogMonitoringInvocation
  public ResponseEntity<RejectionReasonResponse> getRejectionReason(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    return ResponseEntity.ok(rejectionReasonService.getRejectionReasonInfo(applicationFormId));
  }
}
