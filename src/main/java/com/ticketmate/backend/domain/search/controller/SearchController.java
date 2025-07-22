package com.ticketmate.backend.domain.search.controller;

import com.ticketmate.backend.domain.search.domain.dto.request.SearchRequest;
import com.ticketmate.backend.domain.search.domain.dto.response.SearchResponse;
import com.ticketmate.backend.domain.search.service.SearchService;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
  public ResponseEntity<SearchResponse<?>> search(@ParameterObject @Valid SearchRequest request){
    return ResponseEntity.ok(searchService.search(request));
  }
}
