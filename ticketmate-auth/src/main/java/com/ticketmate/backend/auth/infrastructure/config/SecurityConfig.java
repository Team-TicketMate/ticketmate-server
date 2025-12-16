package com.ticketmate.backend.auth.infrastructure.config;

import static com.ticketmate.backend.auth.infrastructure.constant.AuthConstants.OAUTH2_LOGIN_URL;

import com.ticketmate.backend.auth.application.validator.AuthValidator;
import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.auth.infrastructure.admin.CustomAdminUserService;
import com.ticketmate.backend.auth.infrastructure.constant.AuthConstants;
import com.ticketmate.backend.auth.infrastructure.constant.SecurityUrls;
import com.ticketmate.backend.auth.infrastructure.filter.TokenAuthenticationFilter;
import com.ticketmate.backend.auth.infrastructure.handler.CustomLogoutHandler;
import com.ticketmate.backend.auth.infrastructure.handler.CustomLoginSuccessHandler;
import com.ticketmate.backend.auth.infrastructure.handler.CustomLogoutSuccessHandler;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomClientRegistrationRepository;
import com.ticketmate.backend.auth.infrastructure.oauth2.CustomOAuth2UserService;
import com.ticketmate.backend.auth.infrastructure.properties.AuthProperties;
import com.ticketmate.backend.auth.infrastructure.properties.OAuth2Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({AuthProperties.class, OAuth2Properties.class})
public class SecurityConfig {

  private final TokenProvider tokenProvider;
  private final CustomLoginSuccessHandler customLoginSuccessHandler;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomAdminUserService customAdminUserService;
  private final CustomClientRegistrationRepository customClientRegistrationRepository;
  private final CustomLogoutHandler customLogoutHandler;
  private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
  private final AuthValidator authValidator;
  private final PasswordEncoder passwordEncoder;

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
        // AUTH_WHITELIST에 등록된 URL은 인증 허용
        .requestMatchers(SecurityUrls.AUTH_WHITELIST.toArray(new String[0])).permitAll()
        // OPTIONAL_AUTH_PATHS에 등록된 URL도 인증 허용
        .requestMatchers(HttpMethod.GET, SecurityUrls.OPTIONAL_AUTH_PATHS.toArray(new String[0])).permitAll()
        // ADMIN_PATHS에 등록된 URL은 관리자만 접근가능 TODO: 추후 테스트 계정 권한 삭제
        .requestMatchers(SecurityUrls.ADMIN_PATHS.toArray(new String[0])).hasAnyRole("ADMIN", "TEST_ADMIN")
        .anyRequest().authenticated()
      )
      // 로그아웃
      .logout(logout -> logout
        .logoutRequestMatcher(new AntPathRequestMatcher(AuthConstants.LOGOUT_API_PATH, HttpMethod.POST.name())) // LOGOUT_URL 경로로 접근 시 로그아웃
        .addLogoutHandler(customLogoutHandler) // 로그아웃 핸들러 등록 (쿠키 삭제)
        .logoutSuccessHandler(customLogoutSuccessHandler) // 로그아웃 성공시 200OK
      )
      // 세션 설정: STATELESS
      .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      // OAuth2
      .oauth2Login(oauth2 -> oauth2
        .authorizationEndpoint(authorization ->
          authorization.baseUri(OAUTH2_LOGIN_URL)) // oauth2 로그인 엔드포인트
        .clientRegistrationRepository(customClientRegistrationRepository.clientRegistrationRepository())
        .userInfoEndpoint(userInfoEndpointConfig ->
          userInfoEndpointConfig
            .userService(customOAuth2UserService))
        .successHandler(customLoginSuccessHandler))
      // JWT Filter
      .addFilterAfter(
        new TokenAuthenticationFilter(tokenProvider, customOAuth2UserService, authValidator),
        OAuth2LoginAuthenticationFilter.class
      )
      // 관리자 로그인용 DaoAuthenticationProvider
      .authenticationProvider(daoAuthenticationProvider())
      .build();
  }

  /**
   * 관리자 계정 인증 제공자
   */
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(customAdminUserService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }
}
