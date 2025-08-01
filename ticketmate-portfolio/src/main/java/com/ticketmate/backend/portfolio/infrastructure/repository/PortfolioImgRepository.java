package com.ticketmate.backend.portfolio.infrastructure.repository;

import com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioImgRepository extends JpaRepository<PortfolioImg, UUID> {

}
