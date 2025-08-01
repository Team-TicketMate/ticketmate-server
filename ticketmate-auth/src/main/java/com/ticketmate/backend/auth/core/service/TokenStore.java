package com.ticketmate.backend.auth.core.service;

public interface TokenStore {

  /**
   * 리프레시 토큰을 주어진 Key로 저장하고 TTL(ms) 설정
   */
  void save(String key, String refreshToken, long ttlMillis);

  /**
   * Key에 해당하는 리프레시 토큰 삭제
   */
  void remove(String key);
}
