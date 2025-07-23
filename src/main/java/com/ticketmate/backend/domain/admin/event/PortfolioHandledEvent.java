package com.ticketmate.backend.domain.admin.event;

import com.ticketmate.backend.domain.portfolio.domain.constant.PortfolioType;
import java.util.UUID;

public record PortfolioHandledEvent(UUID portfolioId, PortfolioType type) { }
