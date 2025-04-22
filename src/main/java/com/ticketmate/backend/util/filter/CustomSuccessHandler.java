package com.ticketmate.backend.util.filter;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.common.CookieUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CookieUtil cookieUtil;

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

        // state 파라미터에서 redirectUri 추출
        String redirectUri = prodRedirectUri; // 기본값
        String state = request.getParameter("state");
        if (!nvl(state, "").isEmpty()) {
            log.debug("state 파라미터 수신: {}", state);
            try {
                String decodedState = new String(Base64.getUrlDecoder().decode(state));
                if (decodedState.contains("::")) {
                    String[] stateParts = decodedState.split("::");
                    if (stateParts.length > 1 && !stateParts[1].isEmpty()) {
                        redirectUri = stateParts[1];
                        log.debug("state에서 추출한 redirectUri: {}", redirectUri);
                    } else {
                        log.debug("state에 redirectUri가 포함되지 않음. 기본 redirectUri 사용: {}", redirectUri);
                    }
                } else {
                    log.debug("state에 구분자(::)가 없음. 기본 redirectUri 사용: {}", redirectUri);
                }
            } catch (IllegalArgumentException e) {
                log.warn("state 디코딩 실패, 기본 redirectUri 사용: {}. 오류: {}", redirectUri, e.getMessage());
            }
        } else {
            log.debug("state 파라미터가 없으므로 기본 redirectUri 사용: {}", redirectUri);
        }

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
