package com.ticketmate.backend.global.constant;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

/**
 * Security 관련 URL 상수 관리
 */
@UtilityClass
public final class SecurityUrls {

  /**
   * 허용된 CORS Origin 목록 (고정 도메인 - 정확한 매칭)
   */
  public static final List<String> ALLOWED_ORIGINS = List.of(

      // 3000번 포트
      "https://ticketmate.site", // 프론트
      "https://ticketmate-client.vercel.app", // 프론트 배포
      "https://ticketmate-admin.vercel.app", // 프론트 관리자

      "https://www.ticketmate.site", // 프론트 배포
      "https://dev.ticketmate.site", // 프론트 test

      // API
      "https://api.ticketmate.site", // 메인 API 서버
      "https://test.ticketmate.site", // 테스트 API 서버

      // Local
      "http://localhost:8080", // 로컬 API 서버
      "http://localhost:3000", // 로컬 웹 서버
      "http://localhost:3001", // 채팅 테스트 전용 로컬 웹 서버


      "http://10.*:*", // 10.0.0.0/8
      "http://172.*:*", // 172.16.0.0/12 전체 (16~31)
      "http://192.168.*:*" // 192.168.0.0/16
  );
  /**
   * 인증을 생략할 URL 패턴 목록
   */
  public static final List<String> AUTH_WHITELIST = Arrays.asList(
      // API
      "/", // 홈
      "/api/auth/reissue", // accessToken 재발급
      "/api/oauth2/**", // 소셜 로그인
      "/login/**", // 기본 Spring Security OAuth2 로그인경로
      "/api/concert/**", // 공연 조회
      "/api/concert-hall/**", // 공연장 조회

      // TEST
      "/test/**", // 개발자용 테스트 api

      // Swagger
      "/docs/**", // Swagger UI
      "/v3/api-docs/**", // Swagger API 문서

      // FireBase 서비스워커
      "/firebase-messaging-sw.js",

      // fmc test용 정적리소스
      "/notification.html",

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
  public static final List<String> ADMIN_PATHS = Arrays.asList(
      // API
      "/admin/concert-hall/**",
      "/admin/portfolio/**"
  );
}
