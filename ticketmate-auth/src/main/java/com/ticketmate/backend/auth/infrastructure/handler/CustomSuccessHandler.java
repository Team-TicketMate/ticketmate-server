package com.ticketmate.backend.auth.infrastructure.handler;

import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.REFRESH_TOKEN_KEY;

import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.core.service.TokenStore;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.auth.infrastructure.properties.JwtProperties;
import com.ticketmate.backend.auth.infrastructure.util.AuthUtil;
import com.ticketmate.backend.auth.infrastructure.util.CookieUtil;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.infrastructure.domain.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Value("${spring.security.app.redirect-uri.dev}")
  private String devRedirectUri;

  @Value("${spring.security.app.redirect-uri.prod}")
  private String prodRedirectUri;

  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;
  private final JwtProperties jwtProperties;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // CustomOAuth2User
    Member member = ((CustomOAuth2User) authentication.getPrincipal()).getMember();
    String accessToken = tokenProvider.createAccessToken(member.getMemberId().toString(), member.getUsername(), member.getRole().name());
    String refreshToken = tokenProvider.createRefreshToken(member.getMemberId().toString(), member.getUsername(), member.getRole().name());

    log.debug("로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
    log.debug("accessToken = {}", accessToken);
    log.debug("refreshToken = {}", refreshToken);

    // RefreshToken을 Redis에 저장 (key: RT:memberId)
    tokenStore.save(AuthUtil.getRefreshTokenTtlKey(member.getMemberId().toString()), refreshToken, jwtProperties.refreshExpMillis());

    // 쿠키에 accessToken, refreshToken 추가
    response.addCookie(CookieUtil.createCookie(ACCESS_TOKEN_KEY, accessToken, jwtProperties.accessExpMillis() / 1000));
    response.addCookie(CookieUtil.createCookie(REFRESH_TOKEN_KEY, refreshToken, jwtProperties.refreshExpMillis() / 1000));

    // 로그인 성공 후 메인 페이지로 리다이렉트
    try {
      log.debug("로그인 성공, 메인페이지로 리다이렉트 됩니다");
      if (!response.isCommitted()) {
        response.sendRedirect(prodRedirectUri); // FIXME: 추후 리다이렉트 동적으로 받는 로직 작성
      }
    } catch (IOException e) {
      log.error("로그인 성공 후 리다이렉트 과정에서 문제가 발생했습니다. {}", e.getMessage());
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
