package com.ticketmate.backend.review.application.mapper;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.review.application.dto.response.AgentCommentResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewFilteredResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewImgResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewInfoResponse;
import com.ticketmate.backend.review.infrastructure.entity.Review;
import com.ticketmate.backend.review.infrastructure.entity.ReviewImg;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewMapperImpl implements ReviewMapper {
  private final StorageService storageService;

  @Override
  public ReviewInfoResponse toReviewInfoResponse(Review review) {
    return new ReviewInfoResponse(
        review.getReviewId(),
        review.getApplicationForm().getConcert().getConcertName(),
        review.getRating(),
        review.getComment(),
        toReviewImgResponseList(review.getReviewImgList()),
        TimeUtil.toLocalDateTime(review.getCreatedDate()),
        toAgentCommentResponse(review)
    );
  }

  @Override
  public ReviewFilteredResponse toReviewFilteredResponse(Review review) {
    return new ReviewFilteredResponse(
        review.getReviewId(),
        review.getApplicationForm().getConcert().getConcertName(),
        review.getRating(),
        review.getComment(),
        toReviewImgResponseList(review.getReviewImgList()),
        TimeUtil.toLocalDateTime(review.getCreatedDate())
    );
  }

  private List<ReviewImgResponse> toReviewImgResponseList(List<ReviewImg> reviewImgList) {
    if (CommonUtil.nullOrEmpty(reviewImgList)) {
      return List.of();
    }
    return reviewImgList.stream()
        .filter(img -> !CommonUtil.nvl(img.getStoredPath(), "").isEmpty())
        .map(img -> new ReviewImgResponse(
            img.getReviewImgId(),
            storageService.generatePublicUrl(img.getStoredPath())))
        .toList();
  }

  private AgentCommentResponse toAgentCommentResponse(Review review) {
    if (review.getAgentComment() == null) return null;

    String profileStoredPath = review.getAgent().getProfileImgStoredPath();
    String profileUrl = CommonUtil.nvl(profileStoredPath, "").isEmpty()
        ? null : storageService.generatePublicUrl(profileStoredPath);

    return new AgentCommentResponse(
        review.getAgent().getNickname(),
        profileUrl,
        review.getAgentComment(),
        TimeUtil.toLocalDateTime(review.getAgentCommentedDate())
    );
  }
}
