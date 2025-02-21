package com.ticketmate.backend.util.config;

import com.ticketmate.backend.service.member.oauth2.CustomOAuth2UserService;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.filter.CustomSuccessHandler;
import com.ticketmate.backend.util.filter.TokenAuthenticationFilter;
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

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomClientRegistrationRepository customClientRegistrationRepository;

    /**
     * 허용된 CORS Origin 목록
     */
    private static final String[] ALLOWED_ORIGINS = {

            // 3000번 포트
            "https://ticketmate.site", // 프론트

            // API
            "https://api.ticketmate.site", // 메인 API 서버
            "https://test.ticketmate.site", // 테스트 API 서버

            // Local
            "http://localhost:8080", // 로컬 API 서버
            "http://localhost:3000" // 로컬 웹 서버
    };

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
                        .hasAnyRole("ADMIN", "TEST") // ADMIN_PATHS에 등록된 URL은 관리자만 접근가능 TODO: 추후 테스트 계정 권한 삭젠
                        .anyRequest().authenticated()
                )
                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout") // "/logout" 경로로 접근 시 로그아웃
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
        configuration.setAllowedOriginPatterns(Arrays.asList(ALLOWED_ORIGINS));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return urlBasedCorsConfigurationSource;
    }
}
