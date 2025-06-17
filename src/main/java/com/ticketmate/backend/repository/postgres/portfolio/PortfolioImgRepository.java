package com.ticketmate.backend.repository.postgres.portfolio;

import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioImgRepository extends JpaRepository<PortfolioImg, UUID> {

}
