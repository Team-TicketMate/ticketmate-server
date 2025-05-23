package com.ticketmate.backend.service.member;

import com.ticketmate.backend.object.dto.auth.request.CustomOAuth2User;
import com.ticketmate.backend.util.JwtUtil;
import com.ticketmate.backend.util.common.CookieUtil;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CookieUtil cookieUtil;

    private static final String REFRESH_PREFIX = "RT:";

    /**
     * 쿠키에 저장된 refreshToken을 통해 accessToken, refreshToken을 재발급합니다
     */
    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        log.debug("accessToken이 만료되어 재발급을 진행합니다.");
        String refreshToken = null;

        // 쿠키에서 리프레시 토큰 추출
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.error("쿠키가 존재하지 않습니다.");
            throw new CustomException(ErrorCode.COOKIES_NOT_FOUND);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        // 리프레시 토큰이 없는 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            log.error("쿠키에서 refreshToken을 찾을 수 없습니다.");
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 해당 refreshToken이 유효한지 검증
        isValidateRefreshToken(refreshToken);

        // 새로운 accessToken, refreshToken 발급
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) jwtUtil
                .getAuthentication(refreshToken).getPrincipal();
        String newAccessToken = jwtUtil.createAccessToken(customOAuth2User);
        String newRefreshToken = jwtUtil.createRefreshToken(customOAuth2User);

        // 기존 refreshToken 삭제
        if (redisTemplate.delete(REFRESH_PREFIX + customOAuth2User.getMemberId())) {
            log.debug("기존 리프레시 토큰 삭제: {}", customOAuth2User.getMemberId());
        } else {
            log.warn("리프레시 토큰 삭제에 실패했습니다: {}", customOAuth2User.getMemberId());
        }

        // refreshToken 저장
        // RefreshToken을 Redisd에 저장 (key: RT:memberId)
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + customOAuth2User.getMemberId(),
                newRefreshToken,
                jwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );
        log.debug("refreshToken 재발급 및 저장 성공");

        // 쿠키에 refreshToken 추가
        response.addCookie(cookieUtil.createCookie("accessToken", newAccessToken));
        response.addCookie(cookieUtil.createCookie("refreshToken", newRefreshToken));
    }

    /**
     * 요청된 리프레시 토큰이 유효한지 확인하고 유효하다면 해당 리프레시 토큰을 반환합니다.
     */
    private void isValidateRefreshToken(String token) {
        if (jwtUtil.isExpired(token)) { // 리프레시 토큰 만료 여부 확인
            log.error("refreshToken이 만료되었습니다.");
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // 토큰이 refresh인지 확인 (발급 시 페이로드에 명시)
        String category = jwtUtil.getCategory(token);
        if (!category.equals("refresh")) {
            log.error("요청된 토큰이 refreshToken이 아닙니다. 요청된 토큰 카테고리: {}", category);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
