package com.ticketmate.backend.review.infrastructure.repository;

import com.ticketmate.backend.review.infrastructure.entity.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewImgRepository extends JpaRepository<ReviewImg, UUID> {
}
