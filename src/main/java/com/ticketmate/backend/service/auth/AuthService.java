package com.ticketmate.backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIRECT_URI_KEY_PREFIX = "redirect_uri:";
    private static final int EXPIRY_SECONDS = 60; // 1분 TTL

    // redirectUri 저장 및 키 반환
    @Transactional
    public String saveRedirectUri(String redirectUri) {
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
