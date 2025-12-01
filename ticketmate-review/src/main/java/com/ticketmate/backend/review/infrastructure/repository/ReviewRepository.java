package com.ticketmate.backend.review.infrastructure.repository;

import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.review.infrastructure.entity.Review;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
  Boolean existsByFulfillmentForm(FulfillmentForm fulfillmentForm);

  Page<Review> findByAgent(Member agent, Pageable pageable);
}
