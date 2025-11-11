package com.ticketmate.backend.api.application.controller.mock;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.mock.application.dto.request.MockLoginRequest;
import com.ticketmate.backend.mock.application.dto.request.MockNotificationRequest;
import com.ticketmate.backend.mock.application.dto.response.MockChatRoomResponse;
import com.ticketmate.backend.mock.application.dto.response.MockLoginResponse;
import com.ticketmate.backend.mock.application.service.MockService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mock")
@Tag(
  name = "개발자 테스트용 API",
  description = "개발자 편의를 위한 테스트용 API 제공"
)
public class MockController implements MockControllerDocs {

  private final MockService mockService;

  @Override
  @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MockLoginResponse> socialLogin(
    @Valid @ModelAttribute MockLoginRequest request) {
    return ResponseEntity.ok(mockService.testSocialLogin(request));
  }

  @Override
  @PostMapping(value = "/member")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> generateMockMembers(
    @RequestParam @Schema(defaultValue = "30") int count) {
    return mockService.generateMemberMockDataAsync(count)
      .thenApply(v -> ResponseEntity.ok(count + "개의 회원 Mock 데이터 생성 완료"))
      .exceptionally(ex -> {
        throw new RuntimeException("회원 Mock 데이터 생성 실패: " + ex.getMessage());
      });
  }

  @Override
  @DeleteMapping("/member")
  @LogMonitoringInvocation
  public ResponseEntity<Void> deleteTestMember() {
    mockService.deleteTestMember();
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/concert-hall")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> createConcertHallMockData(
    @Schema(defaultValue = "30") Integer count) {
    return mockService.createConcertHallMockData(count)
      .thenApply(v -> ResponseEntity.ok(count + "개의 공연장 Mock 데이터 생성 완료"))
      .exceptionally(ex -> {
        throw new RuntimeException("공연장 Mock 데이터 생성 실패: " + ex.getMessage());
      });
  }

  @Override
  @PostMapping("/concert")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> createConcertMockData(
    @Schema(defaultValue = "30") Integer count) {
    return mockService.generateConcertMockDataAsync(count)
      .thenApply(v -> ResponseEntity.ok(count + "개의 공연 Mock 데이터 생성 완료"))
      .exceptionally(ex -> {
        throw new RuntimeException("공연 Mock 데이터 생성 실패: " + ex.getMessage());
      });
  }

  @Override
  @PostMapping("/application-form")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> createApplicationFormMockData(
    @Schema(defaultValue = "30") Integer count) {
    return mockService.generateApplicationFormMockDataAsync(count)
      .thenApply(v -> ResponseEntity.ok(count + "개의 신청서 Mock 데이터 생성 완료"))
      .exceptionally(ex -> {
        throw new RuntimeException("신청서 Mock 데이터 생성 실패: " + ex.getMessage());
      });
  }

  @Override
  @PostMapping("/portfolio")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> createPortfolioMockData(
    @Schema(defaultValue = "30") Integer count) {
    return mockService.generateMockPortfoliosAsync(count)
      .thenApply(v -> ResponseEntity.ok(count + "개의 포트폴리오 Mock 데이터 생성 완료"))
      .exceptionally(ex -> {
        throw new RuntimeException("포트폴리오 Mock 데이터 생성 실패: " + ex.getMessage());
      });
  }

  @PostMapping("/chat-room")
  @Override
  @LogMonitoringInvocation
  public MockChatRoomResponse createChatRoomMockData() {
    return mockService.createChatRoomMockData();
  }

  @Override
  public void sendTestNotification(
    @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
    @RequestBody MockNotificationRequest request
  ) {
    mockService.generateNotificationMockData(customOAuth2User.getMember(), request);
  }
}
