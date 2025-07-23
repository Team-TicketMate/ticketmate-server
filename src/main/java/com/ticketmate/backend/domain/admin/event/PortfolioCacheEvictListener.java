package com.ticketmate.backend.domain.admin.event;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PortfolioCacheEvictListener {
  /**
   * PortfolioHandledEvent에서 포트폴리오 승인 시 search 캐시 무효화
   */
  @CacheEvict(cacheNames = "searches", allEntries = true)
  @EventListener
  public void onPortfolioHandled(PortfolioHandledEvent event) {
    // AOP가 캐시를 날려주므로 메서드 본문 X
  }
}
