package com.ticketmate.backend.admin.portfolio.infrastructure.event;

import com.ticketmate.backend.portfolio.core.constant.PortfolioType;
import java.util.UUID;

public record PortfolioHandledEvent(UUID portfolioId, PortfolioType type) {

}
