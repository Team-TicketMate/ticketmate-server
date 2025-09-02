package com.ticketmate.backend.search.infrastructure.listener;

import com.ticketmate.backend.search.application.event.SearchEvent;
import com.ticketmate.backend.search.infrastructure.service.RecentSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEventListener {

  private final RecentSearchService recentSearchService;

  @Async
  @EventListener
  public void handleSearchEvent(SearchEvent searchEvent) {
    log.debug("검색어 저장 이벤트 수신 - 사용자 ID: {}, 키워드: '{}'",
        searchEvent.memberId(), searchEvent.keyword());
    recentSearchService.addRecentSearch(searchEvent.memberId(), searchEvent.keyword());
  }
}
