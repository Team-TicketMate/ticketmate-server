package com.ticketmate.backend.controller.concert;

import com.ticketmate.backend.controller.concert.docs.ConcertControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concert.request.ConcertFilteredRequest;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.service.concert.ConcertService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert")
@Tag(
        name = "공연 관련 API",
        description = "공연 관련 API 제공"
)
public class ConcertController implements ConcertControllerDocs {

    private final ConcertService concertService;

    @Override
    @GetMapping(value = "")
    @LogMonitoringInvocation
    public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @ModelAttribute ConcertFilteredRequest request) {
        return ResponseEntity.ok(concertService.filteredConcert(request));
    }
}
