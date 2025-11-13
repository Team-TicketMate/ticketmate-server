package com.ticketmate.backend.api.application.controller.applicationform;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormDuplicateRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormEditRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormFilteredRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRejectRequest;
import com.ticketmate.backend.applicationform.application.dto.request.ApplicationFormRequest;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.applicationform.application.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.applicationform.application.service.ApplicationFormService;
import com.ticketmate.backend.applicationform.application.service.RejectionReasonService;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
  @LogMonitoring
  public ResponseEntity<Void> saveApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestBody @Valid ApplicationFormRequest request) {
    applicationFormService.createApplicationForm(request, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("")
  @LogMonitoring
  public ResponseEntity<Page<ApplicationFormFilteredResponse>> filteredApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @ParameterObject @Valid ApplicationFormFilteredRequest request) {
    return ResponseEntity.ok(applicationFormService.filteredApplicationForm(request));
  }

  @Override
  @GetMapping("/{application-form-id}")
  @LogMonitoring
  public ResponseEntity<ApplicationFormInfoResponse> getApplicationFormInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    return ResponseEntity.ok(applicationFormService.getApplicationFormInfo(applicationFormId));
  }

  @Override
  @PatchMapping("/{application-form-id}/edit")
  @LogMonitoring
  public ResponseEntity<Void> editApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId,
      @RequestBody @Valid ApplicationFormEditRequest applicationFormEditRequest) {
    applicationFormService.editApplicationForm(applicationFormId, applicationFormEditRequest, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping("/{application-form-id}/cancel")
  @LogMonitoring
  public ResponseEntity<Void> cancelApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    applicationFormService.cancelApplicationForm(applicationFormId, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping("/{application-form-id}/reject")
  @LogMonitoring
  public ResponseEntity<Void> rejectApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId,
      @RequestBody @Valid ApplicationFormRejectRequest request) {
    applicationFormService.rejectApplicationForm(applicationFormId, customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping("/{application-form-id}/accept")
  @LogMonitoring
  public ResponseEntity<Void> approve(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    applicationFormService.acceptedApplicationForm(applicationFormId, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/duplicate")
  @LogMonitoring
  public ResponseEntity<Boolean> isDuplicateApplicationForm(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestBody @Valid ApplicationFormDuplicateRequest request) {
    Member client = customOAuth2User.getMember();
    return ResponseEntity.ok(applicationFormService.isDuplicateApplicationForm(client, request));
  }

  @Override
  @GetMapping("/{application-form-id}/rejection-reason")
  @LogMonitoring
  public ResponseEntity<RejectionReasonResponse> getRejectionReason(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable(value = "application-form-id") UUID applicationFormId) {
    return ResponseEntity.ok(rejectionReasonService.getRejectionReasonInfo(applicationFormId));
  }
}
