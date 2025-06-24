package com.ticketmate.backend.global.util.auth;

import static com.ticketmate.backend.global.constant.AuthConstants.HEADER_AUTHORIZATION;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_TOKEN_KEY;
import static com.ticketmate.backend.global.constant.AuthConstants.TOKEN_PREFIX;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class AuthUtil {

  /**
   * HTTP 요청에서 엑세스 토큰을 추출합니다
   */
  public String extractAccessTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
    return extractTokenWithoutBearer(bearerToken);
  }

  /**
   * HTTP 요청에서 리프레시 토큰을 추출합니다
   */
  public String extractRefreshTokenFromRequest(HttpServletRequest request) {
    return CookieUtil.extractedByCookieName(request.getCookies(), REFRESH_TOKEN_KEY).getValue();
  }

  /**
   * "Bearer " 부분은 제거하고 순수 토큰을 획득합니다
   */
  private String extractTokenWithoutBearer(String bearerToken) {
    return Optional.ofNullable(bearerToken)
        .filter(token -> token.startsWith(TOKEN_PREFIX))
        .map(token -> token.substring(7).trim())
        .orElse(null);
  }
}
