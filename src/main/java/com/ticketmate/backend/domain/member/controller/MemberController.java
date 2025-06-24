package com.ticketmate.backend.domain.member.controller;

import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.domain.dto.response.MemberResponse;
import com.ticketmate.backend.domain.member.service.MemberService;
import com.ticketmate.backend.global.aop.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @GetMapping
  @LogMonitoringInvocation
  public ResponseEntity<MemberResponse> getMyPage(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
    MemberResponse response = memberService.getMemberInfo(customOAuth2User.getMember());
    return ResponseEntity.ok().body(response);
  }
}
