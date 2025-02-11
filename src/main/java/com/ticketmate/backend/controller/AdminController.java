package com.ticketmate.backend.controller;

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
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(
        name = "관리자 API",
        description = "관리자 페이지 관련 API 제공"
)
public class AdminController implements AdminControllerDocs{

    private final ConcertHallService concertHallService;

    @Override
    @PostMapping(value = "/concert-hall/save")
    @LogMonitoringInvocation
    public ResponseEntity<Void> saveHallInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ConcertHallInfoRequest request) {
        request.setMember(customUserDetails.getMember());
        concertHallService.saveHallInfo(request);
        return ResponseEntity.ok().build();
    }
}
