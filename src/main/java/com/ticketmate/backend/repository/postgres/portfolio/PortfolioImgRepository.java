package com.ticketmate.backend.repository.postgres.portfolio;

import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PortfolioImgRepository extends JpaRepository<PortfolioImg, UUID> {
}
