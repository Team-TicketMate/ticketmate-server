package com.ticketmate.backend.auth.infrastructure.util;

import com.ticketmate.backend.auth.core.service.TokenStore;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class JwtStore implements TokenStore {

  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public void save(String key, String refreshToken, long ttlMillis) {
    redisTemplate.opsForValue().set(key, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public void remove(String key) {
    redisTemplate.delete(key);
  }
}
