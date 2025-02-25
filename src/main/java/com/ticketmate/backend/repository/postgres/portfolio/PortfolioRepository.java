package com.ticketmate.backend.repository.postgres.portfolio;

import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    Page<Portfolio> findAllByPortfolioType(PortfolioType portfolioType, Pageable pageable);
}