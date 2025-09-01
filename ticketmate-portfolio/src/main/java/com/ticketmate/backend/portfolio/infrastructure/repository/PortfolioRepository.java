package com.ticketmate.backend.portfolio.infrastructure.repository;

import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

}