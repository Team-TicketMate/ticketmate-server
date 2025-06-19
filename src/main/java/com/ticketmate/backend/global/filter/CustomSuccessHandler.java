package com.ticketmate.backend.global.filter;

import static com.ticketmate.backend.global.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.REDIS_REFRESH_KEY_PREFIX;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_TOKEN_KEY;

import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.util.auth.CookieUtil;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;
  private final CookieUtil cookieUtil;
  @Value("${spring.security.app.redirect-uri.dev}")
  private String devRedirectUri;

  @Value("${spring.security.app.redirect-uri.prod}")
  private String prodRedirectUri;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // CustomOAuth2User
    CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String accessToken = jwtUtil.createAccessToken(customOAuth2User);
    String refreshToken = jwtUtil.createRefreshToken(customOAuth2User);

    log.debug("로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
    log.debug("accessToken = {}", accessToken);
    log.debug("refreshToken = {}", refreshToken);

    // RefreshToken을 Redisd에 저장 (key: RT:memberId)
    redisTemplate.opsForValue().set(
        REDIS_REFRESH_KEY_PREFIX + customOAuth2User.getMemberId(),
        refreshToken,
        jwtUtil.getRefreshExpirationTime(),
        TimeUnit.MILLISECONDS
    );

    // 쿠키에 accessToken, refreshToken 추가
    response.addCookie(cookieUtil.createCookie(ACCESS_TOKEN_KEY, accessToken));
    response.addCookie(cookieUtil.createCookie(REFRESH_TOKEN_KEY, refreshToken));

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
