package com.ticketmate.backend.auth.infrastructure.handler;

import com.ticketmate.backend.auth.application.service.JwtManager;
import com.ticketmate.backend.auth.core.dto.TokenPair;
import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.infrastructure.constant.AuthConstants;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2User;
import com.ticketmate.backend.auth.infrastructure.util.CookieUtil;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.infrastructure.entity.Member;
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

  private final TokenProvider tokenProvider;
  private final JwtManager jwtManager;
  @Value("${spring.security.app.redirect-uri.dev}")
  private String devRedirectUri;
  @Value("${spring.security.app.redirect-uri.prod}")
  private String prodRedirectUri;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // CustomOAuth2User
    Member member = ((CustomOAuth2User) authentication.getPrincipal()).getMember();
    TokenPair tokenPair = jwtManager.generateTokenPair(member);

    log.debug("로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
    log.debug("accessToken = {}", tokenPair.accessToken());
    log.debug("refreshToken = {}", tokenPair.refreshToken());

    jwtManager.saveAndAttachTokenPair(member, tokenPair, response);
    response.addCookie(CookieUtil.createCookie(
        AuthConstants.PHONE_NUMBER_VERIFIED_KEY,
        String.valueOf(member.isPhoneNumberVerified()),
        AuthConstants.DEFAULT_COOKIE_EXPIRATION_TIME_IN_SECONDS)
    );
    response.addCookie(CookieUtil.createCookie(
        AuthConstants.INITIAL_PROFILE_SET_KEY,
        String.valueOf(member.isInitialProfileSet()),
        AuthConstants.DEFAULT_COOKIE_EXPIRATION_TIME_IN_SECONDS)
    );

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
