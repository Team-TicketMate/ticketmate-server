package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.SignInRequest;
import com.ticketmate.backend.object.dto.SignUpRequest;
import com.ticketmate.backend.service.MemberService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "인증 관련 API",
        description = "회원 인증 관련 API 제공"
)
public class AuthController implements AuthControllerDocs {

    private final MemberService memberService;

    @Override
    @PostMapping(value = "/api/auth/signup")
    @LogMonitoringInvocation
    public ResponseEntity<ApiResponse<Void>> signUp(
            @Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(memberService.signUp(request));
    }

    @Override
    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<ApiResponse<Void>> signIn(SignInRequest request) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
