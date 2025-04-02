package com.ticketmate.backend.util.filter;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_PREFIX = "RT:";

    @Value("${spring.security.app.redirect-uri.dev}")
    private String devRedirectUri;

    @Value("${spring.security.app.redirect-uri.prod}")
    private String prodRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // CustomOAuth2User
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String accessToken = jwtUtil.createAccessToken(customOAuth2User);
        String refreshToken = jwtUtil.createRefreshToken(customOAuth2User);

        log.debug("로그인 성공: 엑세스 토큰 및 리프레시 토큰 생성");
        log.debug("accessToken = {}", accessToken);
        log.debug("refreshToken = {}", refreshToken);

        // RefreshToken을 Redisd에 저장 (key: RT:memberId)
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + customOAuth2User.getMemberId(),
                refreshToken,
                jwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        // 로그인 쿼리 파라미터 Redirect URI 확인
        String redirectUri = request.getParameter("redirectUri");
        if (redirectUri == null) {
            log.debug("로그인 시 요청된 Redirect URI가 없으므로 기본 경로로 설정합니다.");
            redirectUri = devRedirectUri;
        }
        // Redirect URI에 accessToken 추가
        redirectUri += "?accessToken=" + accessToken;
        log.debug("리다이렉트 URL: {}", redirectUri);


        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge((int) (jwtUtil.getRefreshExpirationTime() / 1000)) // 쿠키 maxAge는 초 단위 이므로, 밀리초를 1000으로 나눔
                .build();
        response.addHeader("Set-Cookie", cookie.toString());


//        // 쿠키에 refreshToken 추가
//        Cookie cookie = new Cookie("refreshToken", refreshToken);
//        cookie.setHttpOnly(true); // HttpOnly 설정
//        cookie.setSecure(true); // FIXME: HTTPS 환경에서는 secure 속성 true로 설정 (현재는 HTTP)
//        cookie.setPath("/");
//        cookie.setAttribute("SameSite", "None"); // 크로스 사이트 허용
//        cookie.setMaxAge((int) (jwtUtil.getRefreshExpirationTime() / 1000)); // 쿠키 maxAge는 초 단위 이므로, 밀리초를 1000으로 나눔
//        response.addCookie(cookie);

        // 로그인 성공 후 메인 페이지로 리다이렉트
        try {
            log.debug("로그인 성공, 메인페이지로 리다이렉트 됩니다");
            if (!response.isCommitted()) {
                response.sendRedirect(redirectUri);
            }
        } catch (IOException e) {
            log.error("로그인 성공 후 리다이렉트 과정에서 문제가 발생했습니다. {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
