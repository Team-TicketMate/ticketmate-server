package com.ticketmate.backend.global.config;

import com.ticketmate.backend.domain.member.service.CustomOAuth2UserService;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import com.ticketmate.backend.global.filter.CustomLogoutHandler;
import com.ticketmate.backend.global.filter.CustomSuccessHandler;
import com.ticketmate.backend.global.filter.TokenAuthenticationFilter;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  /**
   * 허용된 CORS Origin 목록 (고정 도메인 - 정확한 매칭)
   */
  private static final List<String> ALLOWED_ORIGINS = List.of(

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

      "http://10.*:*", // 10.0.0.0/8
      "http://172.*:*", // 172.16.0.0/12 전체 (16~31)
      "http://192.168.*:*" // 192.168.0.0/16
  );
  private final JwtUtil jwtUtil;
  private final CustomSuccessHandler customSuccessHandler;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomClientRegistrationRepository customClientRegistrationRepository;
  private final CustomLogoutHandler customLogoutHandler;

  /**
   * Security Filter Chain 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(SecurityUrls.AUTH_WHITELIST.toArray(new String[0]))
            .permitAll() // AUTH_WHITELIST에 등록된 URL은 인증 허용
            .requestMatchers(SecurityUrls.ADMIN_PATHS.toArray(new String[0]))
            .hasAnyRole("ADMIN", "TEST_ADMIN") // ADMIN_PATHS에 등록된 URL은 관리자만 접근가능 TODO: 추후 테스트 계정 권한 삭젠
            .anyRequest().authenticated()
        )
        // 로그아웃
        .logout(logout -> logout
            .logoutUrl("/logout") // "/logout" 경로로 접근 시 로그아웃
            .addLogoutHandler(customLogoutHandler) // 로그아웃 핸들러 등록 (쿠키 삭제, 블랙리스트)
            .logoutSuccessUrl("/login") // 로그아웃 성공 후 로그인 창 이동
            .invalidateHttpSession(true)
        )
        // 세션 설정: STATELESS
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        // OAuth2
        .oauth2Login(oauth2 -> oauth2
            .clientRegistrationRepository(customClientRegistrationRepository.clientRegistrationRepository())
            .userInfoEndpoint(userInfoEndpointConfig ->
                userInfoEndpointConfig
                    .userService(customOAuth2UserService))
            .successHandler(customSuccessHandler))
        // JWT Filter
        .addFilterAfter(
            new TokenAuthenticationFilter(jwtUtil),
            OAuth2LoginAuthenticationFilter.class
        )
        .build();
  }

  /**
   * 인증 메니저 설정
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration)
      throws Exception {

    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * CORS 설정 소스 빈
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(ALLOWED_ORIGINS);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
    urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
    return urlBasedCorsConfigurationSource;
  }
}
