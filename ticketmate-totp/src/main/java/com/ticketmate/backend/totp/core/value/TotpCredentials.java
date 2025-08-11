package com.ticketmate.backend.totp.core.value;

/**
 * TOTP 시크릿 키 및 OTP 앱 등록용 URL을 담는 값 객체
 */
public record TotpCredentials(
    String secret, // 시크릿 키
    String otpAuthUrl // 시크릿 (secret), 계정 이름(accountName), 발행자(issuer) 가 모두 포함된 URL
) {

}
