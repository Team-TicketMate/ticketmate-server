package com.ticketmate.backend.review.infrastructure.repository;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.review.infrastructure.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
  Boolean existsByApplicationForm(ApplicationForm applicationForm);

  Page<Review> findByAgent(Member agent, Pageable pageable);
}
