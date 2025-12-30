package com.ticketmate.backend.api.application.controller.successhistory;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.application.dto.request.SuccessHistoryFilteredRequest;
import com.ticketmate.backend.application.dto.response.SuccessHistoryResponse;
import com.ticketmate.backend.application.service.SuccessHistoryService;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/success-history")
@RequiredArgsConstructor
@Tag(
  name = "성공내역 API",
  description = "성공내역 관련 API 제공"
)
public class SuccessHistoryController implements SuccessHistoryControllerDocs {

  private final SuccessHistoryService successHistoryService;

  @Override
  @GetMapping("{agent-id}")
  @LogMonitoring
  public ResponseEntity<Slice<SuccessHistoryResponse>> getSuccessHistoryList(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @PathVariable(value = "agent-id") UUID agentId,
    @ParameterObject @Valid SuccessHistoryFilteredRequest request) {
    return ResponseEntity.ok(successHistoryService.getSuccessHistoryList(agentId, request));
  }
}
