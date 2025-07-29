package com.ticketmate.backend.global.config.security;

import com.ticketmate.backend.domain.auth.service.CustomOAuth2UserService;
import com.ticketmate.backend.global.config.beans.CustomClientRegistrationRepository;
import com.ticketmate.backend.global.constant.AuthConstants;
import com.ticketmate.backend.global.constant.SecurityUrls;
import com.ticketmate.backend.global.filter.TokenAuthenticationFilter;
import com.ticketmate.backend.global.handler.CustomSuccessHandler;
import com.ticketmate.backend.global.handler.CustomLogoutHandler;
import com.ticketmate.backend.global.util.auth.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

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
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(SecurityUrls.AUTH_WHITELIST.toArray(new String[0]))
            .permitAll() // AUTH_WHITELIST에 등록된 URL은 인증 허용
            .requestMatchers(SecurityUrls.ADMIN_PATHS.toArray(new String[0]))
            .hasAnyRole("ADMIN", "TEST_ADMIN") // ADMIN_PATHS에 등록된 URL은 관리자만 접근가능 TODO: 추후 테스트 계정 권한 삭제
            .anyRequest().authenticated()
        )
        // 로그아웃
        .logout(logout -> logout
            .logoutUrl(AuthConstants.LOGOUT_API_PATH) // LOGOUT_URL 경로로 접근 시 로그아웃
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
}
