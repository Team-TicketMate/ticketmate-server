package com.ticketmate.backend.controller.admin;

import com.ticketmate.backend.controller.admin.docs.AdminControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.dto.concert.request.ConcertInfoRequest;
import com.ticketmate.backend.object.dto.concerthall.request.ConcertHallInfoRequest;
import com.ticketmate.backend.service.concert.ConcertService;
import com.ticketmate.backend.service.concerthall.ConcertHallService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(
        name = "관리자 API",
        description = "관리자 페이지 관련 API 제공"
)
public class AdminController implements AdminControllerDocs {

    private final ConcertService concertService;
    private final ConcertHallService concertHallService;

    @Override
    @PostMapping(value = "/concert-hall/save")
    @LogMonitoringInvocation
    public ResponseEntity<Void> saveHallInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @RequestBody ConcertHallInfoRequest request) {
        concertHallService.saveHallInfo(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping(value = "/concert/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<Void> saveConcertInfo(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @Valid @ModelAttribute ConcertInfoRequest request) {
        concertService.saveConcertInfo(request);
        return ResponseEntity.ok().build();
    }
}
