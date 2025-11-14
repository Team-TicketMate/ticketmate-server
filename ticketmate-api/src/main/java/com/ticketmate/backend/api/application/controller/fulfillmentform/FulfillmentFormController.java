package com.ticketmate.backend.api.application.controller.fulfillmentform;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.fulfillmentform.application.dto.request.FulfillmentFormInfoRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.request.FulfillmentFormRejectRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.request.FulfillmentFormUpdateRequest;
import com.ticketmate.backend.fulfillmentform.application.dto.response.FulfillmentFormInfoResponse;
import com.ticketmate.backend.fulfillmentform.application.service.FulfillmentFormService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/fulfillment-form")
@Tag(
  name = "성공양식 관련 API",
  description = "성공양식 관련 API 제공"
)
public class FulfillmentFormController implements FulfillmentFormControllerDocs {

  private final FulfillmentFormService fulfillmentFormService;

  @Override
  @PostMapping(value = "/{chat-room-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<UUID> saveFulfillmentFormInfo(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("chat-room-id") String chatRoomId,
    @Valid @ModelAttribute FulfillmentFormInfoRequest request) {
    return ResponseEntity.ok(fulfillmentFormService.saveFulfillmentFormInfo(customOAuth2User.getMember(), chatRoomId, request));
  }

  @Override
  @GetMapping(value = "/{fulfillment-form-id}")
  @LogMonitoring
  public ResponseEntity<FulfillmentFormInfoResponse> getFulfillmentFormInfo(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("fulfillment-form-id") UUID fulfillmentFormId
  ) {
    return ResponseEntity.ok(fulfillmentFormService.getFulfillmentFormInfo(customOAuth2User.getMember(), fulfillmentFormId));
  }

  @Override
  @PatchMapping(value = "/{fulfillment-form-id}/accept")
  @LogMonitoring
  public ResponseEntity<Void> acceptFulfillmentForm(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("fulfillment-form-id") UUID fulfillmentFormId
  ) {
    fulfillmentFormService.acceptFulfillmentForm(customOAuth2User.getMember(), fulfillmentFormId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping(value = "/{fulfillment-form-id}/reject")
  @LogMonitoring
  public ResponseEntity<Void> rejectFulfillmentForm(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("fulfillment-form-id") UUID fulfillmentFormId,
    @RequestBody @Valid FulfillmentFormRejectRequest request
  ) {
    fulfillmentFormService.rejectFulfillmentForm(customOAuth2User.getMember(), fulfillmentFormId, request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PatchMapping(value = "/{fulfillment-form-id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<Void> updateFulfillmentForm(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable("fulfillment-form-id") UUID fulfillmentFormId,
    @ModelAttribute @Valid FulfillmentFormUpdateRequest request
  ) {
    fulfillmentFormService.updateFulfillmentForm(customOAuth2User.getMember(), fulfillmentFormId, request);
    return ResponseEntity.ok().build();
  }
}
