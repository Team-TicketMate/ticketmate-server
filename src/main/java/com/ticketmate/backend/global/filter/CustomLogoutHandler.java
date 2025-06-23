package com.ticketmate.backend.global.filter;

import com.ticketmate.backend.global.constant.AuthConstants;
import com.ticketmate.backend.global.util.auth.AuthUtil;
import com.ticketmate.backend.global.util.auth.CookieUtil;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

  private final JwtUtil jwtUtil;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // 쿠키에서 리프레시 토큰 추출 및 삭제
    String refreshToken = AuthUtil.extractRefreshTokenFromRequest(request);
    jwtUtil.deleteRefreshToken(refreshToken);

    // 쿠키 삭제
    deleteCookieAndAttachToResponse(request, response, AuthConstants.ACCESS_TOKEN_KEY);
    deleteCookieAndAttachToResponse(request, response, AuthConstants.REFRESH_TOKEN_KEY);
  }

  /**
   * cookieName에 해당하는 쿠키를 삭제 후 response에 추가합니다
   */
  private void deleteCookieAndAttachToResponse(HttpServletRequest request, HttpServletResponse response, String cookieName) {
    Cookie cookie = CookieUtil.extractedByCookieName(request.getCookies(), cookieName);
    response.addCookie(CookieUtil.deleteCookie(cookie));
  }
}
