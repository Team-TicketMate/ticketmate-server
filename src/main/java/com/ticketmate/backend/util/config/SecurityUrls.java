package com.ticketmate.backend.util.config;

import java.util.Arrays;
import java.util.List;

/**
 * Security 관련 URL 상수 관리
 */
public class SecurityUrls {

    /**
     * 인증을 생략할 URL 패턴 목록
     */
    public static final List<String> AUTH_WHITELIST = Arrays.asList(
            // API
            "/api/auth/reissue", // accessToken 재발급
            "/api/oauth2/**", // 소셜 로그인
            "/login/**", // 기본 Spring Security OAuth2 로그인경로

            // TEST
            "/test/**", // 개발자용 테스트 api

            // 썸네일 파일 TODO: 추후 url 매핑 관련 규칙 수정
            "/concert/thumbnail/**", // 콘서트 썸네일 파일 경로


            // Swagger
            "/docs/**", // Swagger UI
            "/v3/api-docs/**" // Swagger API 문서

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
