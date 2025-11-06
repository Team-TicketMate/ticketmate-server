package com.ticketmate.backend.auth.infrastructure.constant;

import java.util.List;
import lombok.experimental.UtilityClass;

/**
 * Security 관련 URL 상수 관리
 */
@UtilityClass
public class SecurityUrls {

  /**
   * 인증을 생략할 URL 패턴 목록
   */
  public static final List<String> AUTH_WHITELIST = List.of(

    // AUTH
    "/api/auth/login", // 로그인 (관리자)
    "/api/auth/reissue", // accessToken 재발급
    "/api/auth/2fa/setup", // 2FA setup
    "/api/auth/2fa/setup/verify", // 2FA setup verify
    "/api/auth/2fa/login/verify", // 2FA Login verify
    "/api/auth/2fa/reset", // 2FA Reset
    "/api/oauth2/**", // 소셜 로그인

    // API
    "/api/concert/**", // 공연 조회
    "/api/concert-hall/**", // 공연장 조회

    // MOCK
    "/mock/**", // 개발자용 테스트 api

    // Swagger
    "/docs/swagger-ui/**", // Swagger UI
    "/v3/api-docs/**", // Swagger API 문서

    // FireBase 서비스워커
    "/firebase-messaging-sw.js",

    // FCM test용 정적리소스
    "/notification.html",
    "/ticketmate-logo.png",

    // WebSocket 관련 Url (StompChannelInterceptor 내부에서 검증)
    "/chat/**",

    // 채팅 test용 정적 리소스
    "/chat-jwt.html",
    "/chat-jwt-local.html"
  );

  /**
   * 관리자 권한이 필요한 URL 패턴 목록
   */
  public static final List<String> ADMIN_PATHS = List.of(
    // API
    "/admin/concert/**",
    "/admin/concert-hall/**",
    "/admin/portfolio/**",
    "/admin/cool-sms/**"
  );

  /**
   * 선택적 인증 (비로그인/로그인 모두 지원) URL 패턴 목록
   */
  public static final List<String> OPTIONAL_AUTH_PATHS = List.of(
    // API
    "/api/search"
  );

  /**
   * 전화번호 '미인증' 상태 접근 가능 URL 패턴 목록
   * 로그인 이후 전화번호 인증을 완료하기 위한 API
   */
  public static final List<String> PHONE_VERIFICATION_BYPASS_PATHS = List.of(
    "/api/auth/sms/send-code", // sms 문자 발송
    "/api/auth/sms/verify" // sms 문자 인증
  );

  /**
   * 기본 프로필 '미설정' 상태 접근 가능 URL 패턴 목록
   * 로그인 및 전화번호 인증이 끝난 사용자의 초기 프로필 설정을 위한 API
   */
  public static final List<String> INITIAL_PROFILE_SET_BYPASS_PATHS = List.of(
    "/api/member" // 기본 프로필 설정
  );
}
