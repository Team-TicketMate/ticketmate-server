package com.ticketmate.backend.auth.application.service;

import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.REFRESH_TOKEN_KEY;

import com.ticketmate.backend.auth.core.dto.TokenPair;
import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.core.service.TokenStore;
import com.ticketmate.backend.auth.infrastructure.properties.JwtProperties;
import com.ticketmate.backend.auth.infrastructure.util.AuthUtil;
import com.ticketmate.backend.auth.infrastructure.util.CookieUtil;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtManager {

  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;
  private final JwtProperties jwtProperties;

  /**
   * accessToken, refreshToken Pair 생성
   */
  public TokenPair generateTokenPair(Member member) {
    String accessToken = tokenProvider.createAccessToken(
        member.getMemberId().toString(),
        member.getUsername(),
        member.getRole().name()
    );
    String refreshToken = tokenProvider.createRefreshToken(
        member.getMemberId().toString(),
        member.getUsername(),
        member.getRole().name()
    );
    return new TokenPair(accessToken, refreshToken);
  }

  /**
   * Redis에 RefreshToken 저장 후, 응답 쿠키에 Access/Refresh Token 세팅
   */
  public void saveAndAttachTokenPair(Member member, TokenPair tokenPair, HttpServletResponse response) {
    // RefreshToken을 Redis에 저장 (key: RT:memberId)
    log.debug("refreshToken Redis 저장");
    String key = AuthUtil.getRefreshTokenTtlKey(member.getMemberId().toString());
    tokenStore.save(key, tokenPair.refreshToken(), jwtProperties.refreshExpMillis());

    // 응답 쿠키에 accessToken, refreshToken 추가
    response.addCookie(CookieUtil.createCookie(ACCESS_TOKEN_KEY, tokenPair.accessToken(), jwtProperties.accessExpMillis() / 1000));
    response.addCookie(CookieUtil.createCookie(REFRESH_TOKEN_KEY, tokenPair.refreshToken(), jwtProperties.refreshExpMillis() / 1000));
  }
}
