package com.ticketmate.backend.controller;

import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.SignUpDto;
import com.ticketmate.backend.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "인증 관련 API",
        description = "회원 인증 관련 API 제공"
)
public class AuthController implements AuthControllerDocs {

    private final MemberService memberService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(SignUpDto dto) {
        return ResponseEntity.ok(memberService.signUp(dto));
    }
}
