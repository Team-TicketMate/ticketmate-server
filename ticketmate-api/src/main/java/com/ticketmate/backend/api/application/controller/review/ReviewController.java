package com.ticketmate.backend.api.application.controller.review;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.review.application.dto.request.AgentCommentRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewEditRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewFilteredRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewRequest;
import com.ticketmate.backend.review.application.dto.response.ReviewFilteredResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewInfoResponse;
import com.ticketmate.backend.review.application.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/review")
@Tag(
    name = "리뷰 API",
    description = "리뷰 관련 API 제공"
)
public class ReviewController implements ReviewControllerDocs {
  private final ReviewService reviewService;

  @Override
  @GetMapping("/{review-id}")
  @LogMonitoring
  public ResponseEntity<ReviewInfoResponse> getReviewInfo(@PathVariable(name = "review-id") UUID reviewId) {
    return ResponseEntity.ok(reviewService.getReview(reviewId));
  }

  @Override
  @GetMapping("/agent")
  @LogMonitoring
  public ResponseEntity<Page<ReviewFilteredResponse>> getReviewsByAgent(@ParameterObject @Valid ReviewFilteredRequest request) {
    return ResponseEntity.ok(reviewService.getReviewsByAgent(request));
  }

  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<UUID> createReview(@Valid @ModelAttribute ReviewRequest request,
                                           @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok(reviewService.createReview(request, customOAuth2User.getMember()));
  }

  @Override
  @PatchMapping(value = "/{review-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<Void> updateReview(@PathVariable(name = "review-id") UUID reviewId,
                                           @Valid @ModelAttribute ReviewEditRequest request,
                                           @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    reviewService.updateReview(reviewId, request, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{review-id}/comment")
  @LogMonitoring
  public ResponseEntity<Void> addAgentComment(@PathVariable(name = "review-id") UUID reviewId,
                                              @RequestBody @Valid AgentCommentRequest request,
                                              @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    reviewService.addAgentComment(reviewId, request, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }
}
