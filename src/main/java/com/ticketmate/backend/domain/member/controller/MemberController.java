package com.ticketmate.backend.domain.member.controller;

import com.ticketmate.backend.domain.auth.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.dto.response.MemberInfoResponse;
import com.ticketmate.backend.domain.member.service.MemberService;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(
    name = "회원 관련 API",
    description = "회원 관련 API 제공"
)
@RequestMapping("/api/member")
public class MemberController implements MemberControllerDocs {

  private final MemberService memberService;

  @Override
  @GetMapping
  @LogMonitoringInvocation
  public ResponseEntity<MemberInfoResponse> getMemberInfo(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
    return ResponseEntity.ok().body(memberService.getMemberInfo(customOAuth2User.getMember()));
  }
}
