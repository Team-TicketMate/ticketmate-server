package com.ticketmate.backend.auth.infrastructure.handler;

import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.core.service.TokenStore;
import com.ticketmate.backend.auth.infrastructure.constant.AuthConstants;
import com.ticketmate.backend.auth.infrastructure.util.AuthUtil;
import com.ticketmate.backend.auth.infrastructure.util.CookieUtil;
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

  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // 쿠키에서 리프레시 토큰 추출 및 삭제
    String refreshToken = AuthUtil.extractRefreshTokenFromRequest(request);
    String memberId = tokenProvider.getMemberId(refreshToken);
    tokenStore.remove(AuthUtil.getRefreshTokenTtlKey(memberId));

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
