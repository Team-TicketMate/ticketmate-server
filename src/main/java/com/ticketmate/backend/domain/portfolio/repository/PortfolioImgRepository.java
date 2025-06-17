package com.ticketmate.backend.domain.portfolio.repository;

import com.ticketmate.backend.domain.portfolio.domain.entity.PortfolioImg;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioImgRepository extends JpaRepository<PortfolioImg, UUID> {

}
