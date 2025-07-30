package com.ticketmate.backend.auth.infrastructure.service;

import com.ticketmate.backend.auth.core.service.TokenProvider;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtProvider implements TokenProvider {

  private final SecretKey secretKey;
  private final long accessExpMillis;
  private final long refreshExpMillis;
  private final String issuer;

  @Override
  public String createAccessToken(String memberId, String username, String role) {
    log.debug("엑세스 토큰 생성 중: 회원: {}", customOAuth2User.getUsername());
    return createToken(ACCESS_CATEGORY, memberId, username, role, accessExpMillis);
  }

  @Override
  public String createRefreshToken(String memberId, String username, String role) {
    log.debug("리프래시 토큰 생성 중: 회원: {}", customOAuth2User.getUsername());
    return createToken(REFRESH_CATEGORY, memberId, username, role, refreshExpMillis);
  }

  /**
   * JWT 발급
   */
  private String createToken(String category, String memberId, String username, String role, long expMillis) {
    Date now = new Date();
    return Jwts.builder()
        .subject(memberId)
        .claim("category", category)
        .claim("memberId", memberId)
        .claim("username", username)
        .claim("role", role)
        .issuer(issuer)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expMillis))
        .signWith(secretKey)
        .compact();
  }

  @Override
  public boolean isValidToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(secretKey)
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

  @Override
  public String getMemberId(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("memberId", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  @Override
  public String getUsername(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("username", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  @Override
  public String getRole(String token) {
    return Optional.of(token)
        .map(this::getClaims)
        .map(claims -> claims.get("role", String.class))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_JWT_TOKEN));
  }

  /**
   * 토큰에서 페이로드 (Claim) 추출
   */
  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
