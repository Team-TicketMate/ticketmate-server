package com.ticketmate.backend.global.constant;

public final class AuthConstants {

  private AuthConstants() {
    throw new UnsupportedOperationException("이 유틸리티 클래스는 인스턴스화할 수 없습니다.");
  }

  // Auth
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_AUTHORIZATION = "Authorization";

  // CookieUtil
  public static final String ROOT_DOMAIN = "ticketmate.site";
  public static final String ACCESS_TOKEN_KEY = "accessToken";
  public static final String REFRESH_TOKEN_KEY = "refreshToken";

  // JwtUtil
  public static final String ACCESS_CATEGORY = "access";
  public static final String REFRESH_CATEGORY = "refresh";
  public static final String BLACKLIST_PREFIX = "BL:";
  public static final String BLACKLIST_VALUE = "blacklist";
  public static final String REDIS_REFRESH_KEY_PREFIX = "RT:";

}
