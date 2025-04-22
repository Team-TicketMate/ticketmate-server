package com.ticketmate.backend.service.auth;

import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.ticketmate.backend.util.common.CommonUtil.nvl;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIRECT_URI_KEY_PREFIX = "redirect_uri:";
    private static final int EXPIRY_SECONDS = 60; // 1분 TTL

    @Value("${spring.security.app.redirect-uri.dev}")
    private String devRedirectUri;

    @Value("${spring.security.app.redirect-uri.prod}")
    private String prodRedirectUri;

    @Transactional
    public void handleOAuth2Login(String provider, String redirectUri, HttpSession session, HttpServletResponse response) {
        try {
            // redirectUri가 없는경우
            if (nvl(redirectUri, "").isEmpty()) {
                log.debug("redirectUri가 존재하지 않습니다. 기본 리다이렉트 경로를 설정합니다.");
                redirectUri = prodRedirectUri;
            }
            // redirectUri 검증
            validateRedirectUri(redirectUri);

            // redirectUri를 Redis에 저장
            String redirectUriKey = saveRedirectUri(redirectUri);

            // redis에 저장한 key -> 세션 저장
            session.setAttribute("redirectUriKey", redirectUriKey);

            // OAuth2 로그인 엔드포인트로 리다이렉트
            response.sendRedirect("/oauth2/authorization/" + provider);
        } catch (IOException e) {
            log.error("OAuth2 로그인 엔드포인트 리다이렉트 중 오류가 발생했습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 리다이렉트 URI 검증
    private void validateRedirectUri(String redirectUri) {
        if (redirectUri.equals(devRedirectUri) || redirectUri.equals(prodRedirectUri)) {
            log.error("요청된 redirectUri가 유효하지 않습니다.");
            throw new CustomException(ErrorCode.INVALID_REDIRECT_URI);
        }
    }

    // redirectUri 저장 및 키 반환
    private String saveRedirectUri(String redirectUri) {
        String key = REDIRECT_URI_KEY_PREFIX + UUID.randomUUID();
        redisTemplate.opsForValue().set(key, redirectUri, EXPIRY_SECONDS, TimeUnit.SECONDS);
        log.debug("redirectUri 저장이 완료되었습니다.");
        return key;
    }

    // redirectUri 획득 후 삭제
    @Transactional
    public String getAndDeleteRedirectUri(String key) {
        String redirectUri = redisTemplate.opsForValue().get(key);
        if (redirectUri != null) {
            log.debug("redirectUri 추출 완료. 해당 redirectUri를 삭제합니다");
            redisTemplate.delete(key);
        }
        return redirectUri;
    }
}
