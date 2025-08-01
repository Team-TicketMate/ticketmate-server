package com.ticketmate.backend.auth.core.service;

import java.util.Date;

public interface TokenProvider {

  /**
   * 엑세스 토큰 생성
   */
  String createAccessToken(String memberId, String username, String role);

  /**
   * 리프레시 토큰 생성
   */
  String createRefreshToken(String memberId, String username, String role);

  /**
   * 토큰 유효 검사
   */
  boolean isValidToken(String token);

  /**
   * 토큰에서 memberId 파싱
   */
  String getMemberId(String token);

  /**
   * 토큰에서 username 파싱
   */
  String getUsername(String token);

  /**
   * 토큰에서 role 파싱
   */
  String getRole(String token);

  /**
   * 토큰 만료시간 반환 (ms)
   */
  Date getExpiredAt(String token);
}
