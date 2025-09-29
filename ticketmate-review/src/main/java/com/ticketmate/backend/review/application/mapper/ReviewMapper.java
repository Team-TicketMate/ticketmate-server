package com.ticketmate.backend.review.application.mapper;

import com.ticketmate.backend.review.application.dto.response.ReviewResponse;
import com.ticketmate.backend.review.infrastructure.entity.Review;

public interface ReviewMapper {
  ReviewResponse toReviewResponse(Review review);
}
