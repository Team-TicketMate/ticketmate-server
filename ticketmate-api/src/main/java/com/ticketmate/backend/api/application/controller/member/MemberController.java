package com.ticketmate.backend.api.application.controller.member;

import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.common.application.annotation.LogMonitoringInvocation;
import com.ticketmate.backend.member.application.dto.request.MemberFollowFilteredRequest;
import com.ticketmate.backend.member.application.dto.request.MemberFollowRequest;
import com.ticketmate.backend.member.application.dto.response.MemberFollowResponse;
import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.application.service.MemberFollowService;
import com.ticketmate.backend.member.application.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  private final MemberFollowService memberFollowService;

  @Override
  @GetMapping
  @LogMonitoringInvocation
  public ResponseEntity<MemberInfoResponse> getMemberInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok().body(memberService.getMemberInfo(customOAuth2User.getMember()));
  }

  @Override
  @PostMapping("/follow")
  @LogMonitoringInvocation
  public ResponseEntity<Void> follow(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody MemberFollowRequest request) {
    memberFollowService.follow(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/unfollow")
  @LogMonitoringInvocation
  public ResponseEntity<Void> unfollow(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody MemberFollowRequest request) {
    memberFollowService.unfollow(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/follow/{client-id}")
  @LogMonitoringInvocation
  public ResponseEntity<Slice<MemberFollowResponse>> filteredMemberFollow(
      @PathVariable(name = "client-id") UUID clientId,
      @Valid @ParameterObject MemberFollowFilteredRequest request) {
    return ResponseEntity.ok(memberFollowService.filteredMemberFollow(clientId, request));
  }
}
