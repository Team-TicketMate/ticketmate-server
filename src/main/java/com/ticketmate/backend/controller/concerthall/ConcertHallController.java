package com.ticketmate.backend.controller.concerthall;

import com.ticketmate.backend.controller.concerthall.docs.ConcertHallControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallFilteredRequest;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallInfoResponse;
import com.ticketmate.backend.service.concerthall.ConcertHallService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concert-hall")
@Tag(
        name = "공연장 관련 API",
        description = "공연장 관련 API 제공"
)
public class ConcertHallController implements ConcertHallControllerDocs {

    private final ConcertHallService concertHallService;

    @Override
    @GetMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<Page<ConcertHallFilteredResponse>> filteredConcertHall(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @ModelAttribute ConcertHallFilteredRequest request) {
        return ResponseEntity.ok(concertHallService.filteredConcertHall(request));
    }

    @Override
    @GetMapping(value = "/{concert-hall-id}")
    @LogMonitoringInvocation
    public ResponseEntity<ConcertHallInfoResponse> getConcertHallInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("concert-hall-id")UUID concertHallId) {
        return ResponseEntity.ok(concertHallService.getConcertHallInfo(concertHallId));
    }

}
