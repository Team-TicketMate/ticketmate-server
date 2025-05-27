package com.ticketmate.backend.controller.test;

import com.ticketmate.backend.controller.test.docs.TestControllerDocs;
import com.ticketmate.backend.object.dto.test.request.LoginRequest;
import com.ticketmate.backend.service.test.TestService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Tag(
        name = "개발자 테스트용 API",
        description = "개발자 편의를 위한 테스트용 API 제공"
)
public class TestController implements TestControllerDocs {

    private final TestService testService;

    @Override
    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<String> socialLogin(@Valid @ModelAttribute LoginRequest request) {
        return ResponseEntity.ok(testService.testSocialLogin(request));
    }

    @Override
    @DeleteMapping("/member")
    @LogMonitoringInvocation
    public ResponseEntity<Void> deleteTestMember() {
        testService.deleteTestMember();
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/concert-hall")
    @LogMonitoringInvocation
    public ResponseEntity<Void> createConcertHallMockData(
            @Schema(defaultValue = "30") Integer count) {
        testService.createConcertHallMockData(count);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/concert")
    @LogMonitoringInvocation
    public ResponseEntity<Void> createConcertMockData(
            @Schema(defaultValue = "30") Integer count) {
        testService.createConcertMockData(count);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/application-form")
    @LogMonitoringInvocation
    public ResponseEntity<Void> createApplicationFormMockData(
            @Schema(defaultValue = "30") Integer count) {
        testService.createApplicationMockData(count);
        return ResponseEntity.ok().build();
    }


}
