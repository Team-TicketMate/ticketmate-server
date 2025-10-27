package com.ticketmate.backend.auth.infrastructure.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AuthConstants {

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
  public static final String REDIS_REFRESH_KEY_PREFIX = "RT:";

  // API
  public static final String API_RESPONSE_PREFIX = "/api/";
  public static final String ADMIN_RESPONSE_PREFIX = "/admin/";
  public static final String LOGOUT_API_PATH = "/api/auth/logout";
  public static final String LOGOUT_SUCCESS_URL = "/";

  // SMS
  public static final long SMS_CODE_TTL_MIN = 5;
  public static final String REDIS_VERIFICATION_KEY = "VERIF_CODE:";
  public static final String SMS_VERIFICATION_MESSAGE = "[Ticketmate] 인증번호 [{code}]를 입력해주세요.";

  // TOTP
  public static final long TOTP_PENDING_SECRET_TTL_MIN = 5;
  public static final String TOTP_PENDING_SECRET_KEY_PREFIX = "2FA:PENDING:";
  public static final String PRE_AUTH_KEY_PREFIX = "PRE_AUTH:";
  public static final long PRE_AUTH_TTL_MIN = 5;
  public static final String HEADER_PRE_AUTH = "X-PreAuth-Token";
}
