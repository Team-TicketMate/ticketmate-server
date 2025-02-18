package com.ticketmate.backend.controller.concert;

import com.ticketmate.backend.controller.concert.docs.ConcertControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomUserDetails;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping(value = "/filtered")
    @LogMonitoringInvocation
    public ResponseEntity<Page<ConcertFilteredResponse>> filteredConcert(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ConcertFilteredRequest request) {
        return ResponseEntity.ok(concertService.filteredConcert(request));
    }
}
