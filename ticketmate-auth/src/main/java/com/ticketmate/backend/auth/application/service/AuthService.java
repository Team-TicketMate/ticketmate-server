package com.ticketmate.backend.auth.application.service;

import com.ticketmate.backend.auth.application.dto.request.LoginRequest;
import com.ticketmate.backend.auth.application.dto.response.LoginResponse;
import com.ticketmate.backend.auth.core.dto.TokenPair;
import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.core.service.TokenStore;
import com.ticketmate.backend.auth.infrastructure.admin.CustomAdminUser;
import com.ticketmate.backend.auth.infrastructure.util.AuthUtil;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;
  private final MemberService memberService;
  private final PreAuthTokenManager preAuthTokenManager;
  private final AuthenticationManager authenticationManager;
  private final JwtManager jwtManager;

  /**
   * 로그인 (관리자)
   */
  @Transactional
  public LoginResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    Member member = ((CustomAdminUser) authentication.getPrincipal()).getMember();

    String preAuthToken = preAuthTokenManager.generatePreAuthToken(member); // 임시 토큰 발급

    return new LoginResponse(member.isTotpEnabled(), preAuthToken);
  }

  /**
   * 쿠키에 저장된 refreshToken을 통해 accessToken, refreshToken을 재발급합니다
   */
  @Transactional
  public void reissue(HttpServletRequest request, HttpServletResponse response) {
    log.debug("accessToken이 만료되어 재발급을 진행합니다.");

    // 쿠키에서 리프레시 토큰 추출 및 검증
    String oldRefreshToken = AuthUtil.extractRefreshTokenFromRequest(request);

    // 사용자 정보 조회
    String memberId = tokenProvider.getMemberId(oldRefreshToken);
    Member member = memberService.findMemberById(UUID.fromString(memberId));

    // 새로운 토큰 생성
    TokenPair tokenPair = jwtManager.generateTokenPair(member);

    // 기존 refreshToken 삭제
    tokenStore.remove(oldRefreshToken);

    // RefreshToken 저장 및 응답 쿠키 세팅
    jwtManager.saveAndAttachTokenPair(member, tokenPair, response);
  }
}
