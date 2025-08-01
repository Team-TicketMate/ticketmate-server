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

      // API
      "/api/auth/reissue", // accessToken 재발급
      "/api/auth/send-code", // 인증문자 발송
      "/api/auth/verify", // 인증문자 검증
      "/api/oauth2/**", // 소셜 로그인
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
      "/chat-jwt-local2.html",
      "/chat-jwt-local3.html"
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
}
