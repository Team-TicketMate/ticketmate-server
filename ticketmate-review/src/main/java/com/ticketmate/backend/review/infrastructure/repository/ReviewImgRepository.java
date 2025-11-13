package com.ticketmate.backend.review.infrastructure.repository;

import com.ticketmate.backend.review.infrastructure.entity.ReviewImg;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImgRepository extends JpaRepository<ReviewImg, UUID> {
}
