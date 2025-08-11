package com.ticketmate.backend.totp.core.service;

import com.ticketmate.backend.totp.core.value.TotpCredentials;

/**
 * TOTP (Time-based One-time Password) 생성 및 검증 기능 추상화
 */
public interface TotpService {

  /**
   * TOTP를 위한 시크릿 키 생성 및 otp-auth URL 제공
   *
   * @param accountName 사용자 식별자 (예: 이메일)
   * @return 생성된 시크릿과 otpauth URL
   */
  TotpCredentials generateCredentials(String accountName);

  /**
   * 입력된 OTP 코드가 시크릿 키에 대해 유효한지 검증
   *
   * @param secret Base32 인코딩 된 시크릿 키
   * @param code   사용자 입력 6자리 코드
   */
  boolean verifyCode(String secret, int code);

  /**
   * 기존에 발급된 secret로 AuthUrl 반환
   *
   * @param accountName 사용자 식별자 (예: 이메일)
   * @param secret      Base32 인코딩 된 시크릿 키
   * @return 재생성된 AuthUrl
   */
  String getOtpAuthUrl(String accountName, String secret);

}
