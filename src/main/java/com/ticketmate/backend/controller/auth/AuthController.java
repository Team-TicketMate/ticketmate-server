package com.ticketmate.backend.controller.auth;

import com.ticketmate.backend.controller.auth.docs.AuthControllerDocs;
import com.ticketmate.backend.object.dto.auth.request.SignInRequest;
import com.ticketmate.backend.object.dto.auth.request.SignUpRequest;
import com.ticketmate.backend.service.member.MemberService;
import com.ticketmate.backend.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(
        name = "인증 관련 API",
        description = "회원 인증 관련 API 제공"
)
public class AuthController implements AuthControllerDocs {

    private final MemberService memberService;

    @Override
    @PostMapping(value = "/sign-up")
    @LogMonitoringInvocation
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping(value = "/sign-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LogMonitoringInvocation
    public ResponseEntity<Void> signIn(SignInRequest request) {
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping(value = "/reissue")
    @LogMonitoringInvocation
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        memberService.reissue(request, response);
        return ResponseEntity.ok().build();
    }
}
