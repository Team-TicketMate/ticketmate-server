package com.ticketmate.backend.api.application.controller.search;

import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.search.application.dto.request.SearchRequest;
import com.ticketmate.backend.search.application.dto.response.SearchResponse;
import com.ticketmate.backend.search.application.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
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

  @Override
  @GetMapping
  @LogMonitoringInvocation
  public ResponseEntity<SearchResponse<?>> search(@ParameterObject @Valid SearchRequest request) {
    return ResponseEntity.ok(searchService.search(request));
  }
}
