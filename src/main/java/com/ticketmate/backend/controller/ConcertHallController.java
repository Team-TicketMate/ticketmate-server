package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.ConcertHallInfoRequest;
import com.ticketmate.backend.object.dto.CustomUserDetails;
import com.ticketmate.backend.service.ConcertHallService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/concert-hall")
@RequiredArgsConstructor
@Tag(
        name = "공연장 관련 API",
        description = "공연장 관련 API 제공"
)
public class ConcertHallController implements ConcertHallControllerDocs{

    private final ConcertHallService concertHallService;

    @Override
    @PostMapping(value = "/save")
    @LogMonitoringInvocation
    public ResponseEntity<ApiResponse<Void>> saveHallInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ConcertHallInfoRequest request) {
        request.setMember(customUserDetails.getMember());
        return ResponseEntity.ok(concertHallService.saveHallInfo(request));
    }
}
