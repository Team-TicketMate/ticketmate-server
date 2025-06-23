package com.ticketmate.backend.global.util.auth;

import static com.ticketmate.backend.global.constant.AuthConstants.ACCESS_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.ROOT_DOMAIN;

import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class CookieUtil {

  /**
   * 새로운 쿠키를 발급합니다
   *
   * @return 발급된 쿠키를 반환합니다
   */
  public Cookie createCookie(String key, String token, long expirationTimeInSeconds) {
    if (key.equals(ACCESS_TOKEN_KEY)) {
      return createAccessTokenCookie(token, expirationTimeInSeconds);
    } else if (key.equals(REFRESH_TOKEN_KEY)) {
      return createRefreshTokenCookie(token, expirationTimeInSeconds);
    } else {
      log.error("잘못된 Cookie key가 요청됐습니다. 요청값: {}", key);
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
  }

  /**
   * 쿠키의 setMaxAge를 0으로 설정하여 반홥합니다
   *
   * @param cookie 삭제하고 싶은 쿠키
   */
  public Cookie deleteCookie(Cookie cookie) {
    cookie.setMaxAge(0);
    cookie.setPath("/");
    return cookie;
  }

  /**
   * Cookie[]에서 특정 cookieName의 쿠키를 반환합니다
   */
  public Cookie extractedByCookieName(Cookie[] cookies, String cookieName) {
    if (cookies == null) {
      throw new CustomException(ErrorCode.COOKIES_NOT_FOUND);
    }
    return Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals(cookieName))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
  }

  /**
   * 엑세스 토큰이 들어있는 쿠키를 발급합니다
   * httpOnly = false
   */
  private Cookie createAccessTokenCookie(String accessToken, long expirationTimeInSeconds) {
    log.debug("accessToken을 포함한 쿠키를 발급합니다.");
    Cookie cookie = new Cookie(ACCESS_TOKEN_KEY, accessToken);
    cookie.setHttpOnly(false);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setDomain(ROOT_DOMAIN);
    cookie.setMaxAge((int) expirationTimeInSeconds);
    return cookie;
  }

  /**
   * 리프레시 토큰이 들어있는 쿠키를 발급합니다.
   * httpOnly = true
   */
  private Cookie createRefreshTokenCookie(String refreshToken, long expirationTimeInSeconds) {
    log.debug("refreshToken을 포함한 쿠키를 발급합니다.");
    Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setDomain(ROOT_DOMAIN);
    cookie.setMaxAge((int) expirationTimeInSeconds);
    return cookie;
  }
}
