package com.ticketmate.backend.util.filter;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.service.auth.AuthService;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.common.CookieUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CookieUtil cookieUtil;

    private static final String REFRESH_PREFIX = "RT:";

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

        HttpSession session = request.getSession();
        String redirectUriKey = (String) session.getAttribute("redirectUriKey");
        log.debug("세션에서 리다이렉트 KEY를 추출합니다: {}", redirectUriKey);

        if (nvl(redirectUriKey, "").isEmpty()) {
            log.error("세션에 저장된 리다이렉트 키가 없습니다.");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Redis에서 redirectUri 조회
        String redirectUri = authService.getAndDeleteRedirectUri(redirectUriKey);
        if (nvl(redirectUri, "").isEmpty()) {
            log.error("redirectUri가 존재하지 않습니다.");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        log.debug("요청된 redirectUri: {}", redirectUri);

        // 세션에서 키 제거
        session.removeAttribute("redirectUriKey");

        // 쿠키에 accessToken, refreshToken 추가
        response.addCookie(cookieUtil.createCookie("accessToken", accessToken));
        response.addCookie(cookieUtil.createCookie("refreshToken", refreshToken));

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
