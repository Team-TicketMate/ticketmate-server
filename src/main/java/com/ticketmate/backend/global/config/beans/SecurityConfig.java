package com.ticketmate.backend.global.config.beans;

import static com.ticketmate.backend.global.constant.SecurityUrls.ALLOWED_ORIGINS;

import com.ticketmate.backend.domain.auth.service.CustomOAuth2UserService;
import com.ticketmate.backend.global.constant.AuthConstants;
import com.ticketmate.backend.global.constant.SecurityUrls;
import com.ticketmate.backend.global.filter.CustomLogoutHandler;
import com.ticketmate.backend.global.filter.CustomSuccessHandler;
import com.ticketmate.backend.global.filter.TokenAuthenticationFilter;
import com.ticketmate.backend.global.util.auth.JwtUtil;
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
            .logoutUrl(AuthConstants.LOGOUT_URL) // LOGOUT_URL 경로로 접근 시 로그아웃
            .addLogoutHandler(customLogoutHandler) // 로그아웃 핸들러 등록 (쿠키 삭제)
            .logoutSuccessUrl(AuthConstants.LOGOUT_SUCCESS_URL) // 로그아웃 성공 후 페이지 이동
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
