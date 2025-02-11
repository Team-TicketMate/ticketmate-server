package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.ConcertHallFilteredRequest;
import com.ticketmate.backend.object.dto.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.CustomUserDetails;
import com.ticketmate.backend.service.ConcertHallService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert-hall")
@Tag(
        name = "공연장 관련 API",
        description = "공연장 관련 API 제공"
)
public class ConcertHallController implements ConcertHallControllerDocs{

    private final ConcertHallService concertHallService;

    @Override
    @PostMapping(value = "/filtered")
    @LogMonitoringInvocation
    public ResponseEntity<Page<ConcertHallFilteredResponse>> filteredConcertHall(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ConcertHallFilteredRequest request) {
        return ResponseEntity.ok(concertHallService.filteredConcertHall(request));
    }
}
