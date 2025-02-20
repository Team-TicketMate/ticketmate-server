package com.ticketmate.backend.util.filter;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // CustomOAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        Member member = customUserDetails.getMember();
        String accessToken = jwtUtil.createAccessToken(customUserDetails);
        String refreshToken = jwtUtil.createRefreshToken(customUserDetails);

        log.debug("로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
        log.debug("accessToken = {}", accessToken);
        log.debug("refreshToken = {}", refreshToken);

        // RefreshToken을 Redisd에 저장 (key: RT:memberId)
        redisTemplate.opsForValue().set(
                "RT:" + customUserDetails.getMemberId(),
                refreshToken,
                jwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        // 헤더에 accessToken 추가
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 쿠키에 refreshToken 추가
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // HttpOnly 설정
        cookie.setSecure(true); // FIXME: HTTPS 환경에서는 secure 속성 true로 설정 (현재는 HTTP)
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.getRefreshExpirationTime() / 1000)); // 쿠키 maxAge는 초 단위 이므로, 밀리초를 1000으로 나눔
        response.addCookie(cookie);
    }
}
