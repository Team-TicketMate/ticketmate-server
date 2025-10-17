package com.ticketmate.backend.review.application.mapper;

import com.ticketmate.backend.review.application.dto.response.ReviewFilteredResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewInfoResponse;
import com.ticketmate.backend.review.infrastructure.entity.Review;

public interface ReviewMapper {
  ReviewInfoResponse toReviewInfoResponse(Review review);

  ReviewFilteredResponse toReviewFilteredResponse(Review review);
}
