package com.ticketmate.backend.api.application.controller.review;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.review.application.dto.request.ReviewEditRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewRequest;
import com.ticketmate.backend.review.application.dto.response.ReviewResponse;
import com.ticketmate.backend.review.application.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
@Tag(
    name = "리뷰 API",
    description = "리뷰 관련 API 제공"
)
public class ReviewController {
  private final ReviewService reviewService;

  @GetMapping("/{review-id}")
  @LogMonitoringInvocation
  public ResponseEntity<ReviewResponse> getReview(@PathVariable(name = "review-id") UUID reviewId) {
    return ResponseEntity.ok(reviewService.getReview(reviewId));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<UUID> createReview(@Valid @ModelAttribute ReviewRequest request,
                                           @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok(reviewService.createReview(request, customOAuth2User.getMember()));
  }

  @PatchMapping(value = "/{review-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<Void> updateReview(@PathVariable(name = "review-id") UUID reviewId,
                                           @Valid @ModelAttribute ReviewEditRequest request,
                                           @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    reviewService.updateReview(reviewId, request, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("{review-id}")
  @LogMonitoringInvocation
  public ResponseEntity<Void> deleteReview(@PathVariable(name = "review-id") UUID reviewId,
                                           @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    reviewService.deleteReview(reviewId, customOAuth2User.getMember());
    return ResponseEntity.noContent().build();
  }
}
