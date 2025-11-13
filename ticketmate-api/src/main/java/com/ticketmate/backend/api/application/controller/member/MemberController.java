package com.ticketmate.backend.api.application.controller.member;

import com.chuseok22.logging.annotation.LogMonitoring;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.member.application.dto.request.AgentSaveBankAccountRequest;
import com.ticketmate.backend.member.application.dto.request.AgentUpdateBankAccountRequest;
import com.ticketmate.backend.member.application.dto.request.MemberFollowFilteredRequest;
import com.ticketmate.backend.member.application.dto.request.MemberFollowRequest;
import com.ticketmate.backend.member.application.dto.request.MemberInfoUpdateRequest;
import com.ticketmate.backend.member.application.dto.request.MemberWithdrawRequest;
import com.ticketmate.backend.member.application.dto.response.AgentBankAccountResponse;
import com.ticketmate.backend.member.application.dto.response.MemberFollowResponse;
import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.application.service.AgentBankAccountService;
import com.ticketmate.backend.member.application.service.MemberFollowService;
import com.ticketmate.backend.member.application.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  private final AgentBankAccountService agentBankAccountService;

  @Override
  @GetMapping
  @LogMonitoring
  public ResponseEntity<MemberInfoResponse> getMemberInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok().body(memberService.getMemberInfo(customOAuth2User.getMember()));
  }

  @Override
  @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoring
  public ResponseEntity<Void> updateMemberInfo(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @ModelAttribute MemberInfoUpdateRequest request) {
    memberService.updateMemberInfo(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/follow")
  @LogMonitoring
  public ResponseEntity<Void> follow(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody MemberFollowRequest request) {
    memberFollowService.follow(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/unfollow")
  @LogMonitoring
  public ResponseEntity<Void> unfollow(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody MemberFollowRequest request) {
    memberFollowService.unfollow(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/follow/{client-id}")
  @LogMonitoring
  public ResponseEntity<Slice<MemberFollowResponse>> filteredMemberFollow(
      @PathVariable(name = "client-id") UUID clientId,
      @Valid @ParameterObject MemberFollowFilteredRequest request) {
    return ResponseEntity.ok(memberFollowService.filteredMemberFollow(clientId, request));
  }

  @Override
  @PostMapping("/bank-account")
  @LogMonitoring
  public ResponseEntity<Void> saveBankAccount(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody AgentSaveBankAccountRequest request) {
    agentBankAccountService.saveAgentBankAccount(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/bank-account")
  @LogMonitoring
  public ResponseEntity<List<AgentBankAccountResponse>> getBankAccountList(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    return ResponseEntity.ok(agentBankAccountService.getAgentBankAccountList(customOAuth2User.getMember()));
  }

  @Override
  @PatchMapping("/bank-account/{bank-account-id}")
  @LogMonitoring
  public ResponseEntity<Void> changePrimaryBankAccount(
      @PathVariable(name = "bank-account-id") UUID agentBankAccountId,
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    agentBankAccountService.changePrimaryAccount(agentBankAccountId, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PutMapping("/bank-account/{bank-account-id}")
  @LogMonitoring
  public ResponseEntity<Void> changeBankAccountInfo(
      @PathVariable(name = "bank-account-id") UUID agentBankAccountId,
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody AgentUpdateBankAccountRequest request) {
    agentBankAccountService.changeAccountInfo(agentBankAccountId, customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }

  @Override
  @DeleteMapping("/bank-account/{bank-account-id}")
  @LogMonitoring
  public ResponseEntity<Void> deleteBankAccount(
      @PathVariable(name = "bank-account-id") UUID agentBankAccountId,
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
    agentBankAccountService.deleteBankAccount(agentBankAccountId, customOAuth2User.getMember());
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/withdraw")
  @LogMonitoring
  public ResponseEntity<Void> withdraw(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @Valid @RequestBody MemberWithdrawRequest request) {
    memberService.withdraw(customOAuth2User.getMember(), request);
    return ResponseEntity.ok().build();
  }
}
