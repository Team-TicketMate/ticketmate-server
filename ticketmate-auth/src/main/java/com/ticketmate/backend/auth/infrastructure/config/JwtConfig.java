package com.ticketmate.backend.auth.infrastructure.config;

import com.ticketmate.backend.auth.infrastructure.properties.JwtProperties;
import com.ticketmate.backend.auth.infrastructure.service.JwtProvider;
import com.ticketmate.backend.auth.infrastructure.service.JwtStore;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

  private final JwtProperties properties;

  /**
   * JWT 서명에 사용할 키 생성
   * base64로 인코딩 된 시크릿 키를 디코딩해서 SecretKey 객체로 생성
   */
  @Bean
  public SecretKey jwtSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(properties.secretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * TokenProvider 구현체(JwtProvider) 빈 등록
   * 토큰 생성 & 검증 로직을 담은 유틸리티 빈
   */
  @Bean
  public JwtProvider jwtProvider(SecretKey jwtSecretKey) {
    return new JwtProvider(
        jwtSecretKey,
        properties.accessExpMillis(),
        properties.refreshExpMillis(),
        properties.issuer()
    );
  }

  /**
   * TokenStore 구현체(JwtStore) 빈 등록
   * 토큰 저장 & 삭제 로직을 담은 유틸리티 빈
   */
  @Bean
  public JwtStore jwtStore(RedisTemplate<String, String> redisTemplate) {
    return new JwtStore(redisTemplate);
  }
}
