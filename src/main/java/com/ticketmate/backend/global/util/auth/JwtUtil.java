package com.ticketmate.backend.global.util.auth;

import static com.ticketmate.backend.global.constant.AuthConstants.ACCESS_CATEGORY;
import static com.ticketmate.backend.global.constant.AuthConstants.REDIS_REFRESH_KEY_PREFIX;
import static com.ticketmate.backend.global.constant.AuthConstants.REFRESH_CATEGORY;

import com.ticketmate.backend.domain.member.domain.dto.CustomOAuth2User;
import com.ticketmate.backend.domain.member.service.CustomOAuth2UserService;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${jwt.secret-key}")
  private String secretKey;
  @Value("${jwt.access-exp-time}")
  private Long accessTokenExpTime; // AccessToken 만료 시간
  @Value("${jwt.refresh-exp-time}")
  private Long refreshTokenExpTime; // RefreshToken 만료 시간
  @Value("${jwt.issuer}")
  private String issuer; // JWT 발급자

  // 토큰에서 username 파싱
  public String getUsername(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("username", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  // 토큰에서 role 파싱
  public String getRole(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("role", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  // 토큰에서 memberId 파싱
  public String getMemberId(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("memberId", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  // 토큰 만료 여부 확인
  public Boolean isExpired(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(Claims::getExpiration)
        .map(date -> date.before(new Date()))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  // Access/Refresh 토큰 여부
  public String getCategory(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("category", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  /**
   * AccessToken 생성
   *
   * @param customOAuth2User
   * @return
   */
  public String createAccessToken(CustomOAuth2User customOAuth2User) {
    log.debug("엑세스 토큰 생성 중: 회원: {}", customOAuth2User.getUsername());
    return createToken(ACCESS_CATEGORY, customOAuth2User, accessTokenExpTime);
  }

  /**
   * RefreshToken 생성
   *
   * @param customOAuth2User
   * @return
   */
  public String createRefreshToken(CustomOAuth2User customOAuth2User) {
    log.debug("리프래시 토큰 생성 중: 회원: {}", customOAuth2User.getUsername());
    return createToken(REFRESH_CATEGORY, customOAuth2User, refreshTokenExpTime);
  }

  /**
   * JWT 토큰 생성 메서드
   *
   * @param customOAuth2User 회원 상세 정보
   * @param expiredAt        만료 시간
   * @return 생성된 JWT 토큰
   */
  private String createToken(String category, CustomOAuth2User customOAuth2User, Long expiredAt) {

    return Jwts.builder()
        .subject(customOAuth2User.getUsername())
        .claim("category", category)
        .claim("username", customOAuth2User.getUsername())
        .claim("memberId", customOAuth2User.getMemberId())
        .claim("role", customOAuth2User.getMember().getRole())
        .issuer(issuer)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiredAt))
        .signWith(getSignKey())
        .compact();
  }

  /**
   * JWT 토큰 유효성 검사
   *
   * @param token 검증할 JWT 토큰
   * @return 유효 여부
   */
  public boolean isValidToken(String token) throws ExpiredJwtException {
    try {
      Jwts.parser()
          .verifyWith(getSignKey())
          .build()
          .parseSignedClaims(token);
      log.debug("JWT 토큰이 유효합니다.");
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
      throw e; // 만료된 토큰 예외를 호출한 쪽으로 전달
    } catch (UnsupportedJwtException e) {
      log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.warn("형식이 잘못된 JWT 토큰입니다: {}", e.getMessage());
    } catch (SignatureException e) {
      log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
    }
    return false;
  }

  /**
   * JWT 서명에 사용할 키 생성
   *
   * @return SecretKey 객체
   */
  private SecretKey getSignKey() {
    try {
      // Base64 문자열로부터 SecretKey를 생성
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (IllegalArgumentException e) {
      log.error("비밀 키 생성 실패: {}", e.getMessage());
      throw e; // 예외 재발생
    }
  }

  /**
   * JWT 토큰에서 클레임 (Claims) 추출
   *
   * @param token JWT 토큰
   * @return 추출된 클레임
   */
  public Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * token의 남은 유효기간(밀리초)를 반환합니다.
   */
  public long getRemainingValidationMilliSecond(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(Claims::getExpiration)
        .map(expiration -> expiration.getTime() - System.currentTimeMillis())
        .filter(remaining -> remaining > 0)
        .orElse(0L);
  }

  /**
   * 엑세스 토큰 만료 시간 반환 (밀리초 단위)
   */
  public long getAccessExpirationTimeInMilliseconds() {
    return accessTokenExpTime;
  }

  /**
   * 엑세스 토큰 만료 시간 반환 (초 단위)
   */
  public long getAccessExpirationTimeInSeconds() {
    return accessTokenExpTime / 1000;
  }

  /**
   * 리프레시 토큰 만료 시간 반환 (밀리초 단위)
   */
  public long getRefreshExpirationTimeInMilliseconds() {
    return refreshTokenExpTime;
  }

  public long getRefreshExpirationTimeInSeconds() {
    return refreshTokenExpTime / 1000;
  }

  /**
   * 리프레시 토큰 만료 날짜 반환
   *
   * @return 리프레시 토큰 만료 날짜
   */
  public LocalDateTime getRefreshExpiryDate() {
    return LocalDateTime.now().plusSeconds(refreshTokenExpTime / 1000);
  }

  /**
   * JWT 토큰에서 CustomOAuth2User 반환
   *
   * @param token JWT 토큰
   * @return CustomOAuth2User
   */
  public CustomOAuth2User getCustomOAuth2User(String token) {
    return Optional.of(token)
        .map(this::getUsername)
        .map(customOAuth2UserService::loadUserByUsername)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  /**
   * JWT 토큰에서 Authentication 객체 생성
   *
   * @param token JWT 토큰
   * @return Authentication 객체
   */
  public Authentication getAuthentication(String token) {
    return Optional.of(token)
        .map(this::getCustomOAuth2User)
        .map(customOAuth2User -> new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities()))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  /**
   * RefreshToken Redis TTL 저장
   *
   * @param key          리프레시 토큰 키
   * @param refreshToken 저장을 원하는 리프레시 토큰
   */
  public void saveRefreshToken(String key, String refreshToken) {
    redisTemplate.opsForValue().set(
        key,
        refreshToken,
        getRefreshExpirationTimeInMilliseconds(),
        TimeUnit.MILLISECONDS
    );
    log.debug("새로운 리프레시 토큰 저장 성공");
  }

  /**
   * Redis TTL 로 저장된 리프레시 토큰을 삭제합니다
   *
   * @param refreshToken 삭제를 원하는 리프레시 토큰
   */
  public void deleteRefreshToken(String refreshToken) {
    Optional.of(refreshToken)
        .map(this::getMemberId)
        .map(memberId -> redisTemplate.delete(REDIS_REFRESH_KEY_PREFIX + memberId))
        .ifPresent(deleted -> log.debug("리프레시 토큰 삭제 완료"));
  }
}
