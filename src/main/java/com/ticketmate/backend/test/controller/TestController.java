package com.ticketmate.backend.test.controller;

import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import com.ticketmate.backend.test.dto.request.LoginRequest;
import com.ticketmate.backend.test.dto.response.LoginResponse;
import com.ticketmate.backend.test.service.TestService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Tag(
    name = "개발자 테스트용 API",
    description = "개발자 편의를 위한 테스트용 API 제공"
)
public class TestController implements TestControllerDocs {

  private final TestService testService;

  @Override
  @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<LoginResponse> socialLogin(
      @Valid @ModelAttribute LoginRequest request) {
    return ResponseEntity.ok(testService.testSocialLogin(request));
  }

  @Override
  @PostMapping(value = "/member")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> generateMockMembers(
      @RequestParam @Schema(defaultValue = "30") int count) {
    return testService.generateMemberMockDataAsync(count)
        .thenApply(v -> ResponseEntity.ok(count + "개의 회원 Mock 데이터 생성 완료"))
        .exceptionally(ex -> {
          throw new RuntimeException("회원 Mock 데이터 생성 실패: " + ex.getMessage());
        });
  }

  @Override
  @DeleteMapping("/member")
  @LogMonitoringInvocation
  public ResponseEntity<Void> deleteTestMember() {
    testService.deleteTestMember();
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/concert-hall")
  @LogMonitoringInvocation
  public CompletableFuture<ResponseEntity<String>> createConcertHallMockData(
      @Schema(defaultValue = "30") Integer count) {
    return testService.createConcertHallMockData(count)
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
    return testService.generateConcertMockDataAsync(count)
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
    return testService.generateApplicationFormMockDataAsync(count)
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
    return testService.generateMockPortfoliosAsync(count)
        .thenApply(v -> ResponseEntity.ok(count + "개의 포트폴리오 Mock 데이터 생성 완료"))
        .exceptionally(ex -> {
          throw new RuntimeException("포트폴리오 Mock 데이터 생성 실패: " + ex.getMessage());
        });
  }
}
