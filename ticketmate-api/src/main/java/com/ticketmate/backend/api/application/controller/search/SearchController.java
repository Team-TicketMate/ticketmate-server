package com.ticketmate.backend.api.application.controller.search;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.search.application.dto.request.SearchRequest;
import com.ticketmate.backend.search.application.dto.response.SearchResponse;
import com.ticketmate.backend.search.application.service.SearchService;
import com.ticketmate.backend.search.infrastructure.service.RecentSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(
    name = "검색 API",
    description = "공연/대리인 검색 관련 API 제공"
)
public class SearchController implements SearchControllerDocs {

  private final SearchService searchService;
  private final RecentSearchService recentSearchService;

  @Override
  @GetMapping
  @LogMonitoringInvocation
  public ResponseEntity<SearchResponse<?>> search(
      @ParameterObject @Valid SearchRequest request,
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    UUID memberId = (customOAuth2User != null && customOAuth2User.getMember() != null)
        ? customOAuth2User.getMember().getMemberId()
        : null;
    return ResponseEntity.ok(searchService.search(request, memberId));
  }

  @Override
  @GetMapping("/recent")
  @LogMonitoringInvocation
  public ResponseEntity<List<String>> getRecentSearch(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok(recentSearchService.getRecentSearches(customOAuth2User.getMember().getMemberId()));
  }

  @Override
  @DeleteMapping("/recent")
  @LogMonitoringInvocation
  public ResponseEntity<Void> deleteAllRecentSearch(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    recentSearchService.deleteAllRecentSearches(customOAuth2User.getMember().getMemberId());
    return ResponseEntity.noContent().build();
  }
}
