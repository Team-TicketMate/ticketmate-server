package com.ticketmate.backend.domain.search.controller;

import com.ticketmate.backend.domain.search.domain.dto.request.SearchRequest;
import com.ticketmate.backend.domain.search.domain.dto.response.SearchResponse;
import com.ticketmate.backend.domain.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController implements SearchControllerDocs {
  private final SearchService searchService;

  @GetMapping
  public ResponseEntity<SearchResponse<?>> search(@ModelAttribute @Valid SearchRequest request){
    return ResponseEntity.ok(searchService.search(request));
  }
}
