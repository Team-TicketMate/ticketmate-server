package com.ticketmate.backend.global.util.auth;

import static com.ticketmate.backend.global.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.ROOT_DOMAIN;

import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CookieUtil {

  private final JwtUtil jwtUtil;

  /**
   * 새로운 쿠키를 발급합니다
   *
   * @return 발급된 쿠키를 반환합니다
   */
  @Transactional
  public Cookie createCookie(String key, String token) {
    if (key.equals(ACCESS_TOKEN_KEY)) {
      return createAccessTokenCookie(token);
    } else if (key.equals(REFRESH_TOKEN_KEY)) {
      return createRefreshTokenCookie(token);
    } else {
      log.error("잘못된 Cookie key가 요청됐습니다. 요청값: {}", key);
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
  }

  /**
   * 엑세스 토큰이 들어있는 쿠키를 발급합니다
   * httpOnly = false
   *
   * @param accessToken
   * @return
   */
  private Cookie createAccessTokenCookie(String accessToken) {
    log.debug("accessToken을 포함한 쿠키를 발급합니다.");
    Cookie cookie = new Cookie(ACCESS_TOKEN_KEY, accessToken);
    cookie.setHttpOnly(false);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setDomain(ROOT_DOMAIN);
    cookie.setMaxAge((int) (jwtUtil.getAccessExpirationTime() / 1000));
    return cookie;
  }

  /**
   * 리프레시 토큰이 들어있는 쿠키를 발급합니다.
   * httpOnly = true
   *
   * @param refreshToken
   * @return
   */
  private Cookie createRefreshTokenCookie(String refreshToken) {
    log.debug("refreshToken을 포함한 쿠키를 발급합니다.");
    Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setDomain(ROOT_DOMAIN);
    cookie.setMaxAge((int) (jwtUtil.getRefreshExpirationTime() / 1000));
    return cookie;
  }
}
