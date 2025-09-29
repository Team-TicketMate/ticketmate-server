package com.ticketmate.backend.review.application.mapper;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.review.application.dto.response.ReviewImgResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewResponse;
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
  public ReviewResponse toReviewResponse(Review review) {
    return new ReviewResponse(
        review.getReviewId(),
        review.getApplicationForm().getConcert().getConcertName(),
        review.getRating(),
        review.getComment(),
        toImageUrls(review.getReviewImgList()),
        TimeUtil.toLocalDateTime(review.getCreatedDate())
    );
  }

  private List<ReviewImgResponse> toImageUrls(List<ReviewImg> reviewImgList) {
    if (reviewImgList == null || reviewImgList.isEmpty()) {
      return List.of();
    }
    return reviewImgList.stream()
        .filter(img -> !CommonUtil.nvl(img.getStoredPath(), "").isEmpty())
        .map(img -> new ReviewImgResponse(
            img.getReviewImgId(),
            storageService.generatePublicUrl(img.getStoredPath())))
        .toList();
  }

}
