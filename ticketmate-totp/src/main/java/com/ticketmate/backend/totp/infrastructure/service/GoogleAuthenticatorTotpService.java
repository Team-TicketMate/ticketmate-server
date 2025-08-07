package com.ticketmate.backend.totp.infrastructure.service;

import com.ticketmate.backend.totp.core.service.TotpService;
import com.ticketmate.backend.totp.core.value.TotpCredentials;
import com.ticketmate.backend.totp.infrastructure.properties.TotpProperties;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorTotpService implements TotpService {

  private final GoogleAuthenticator googleAuthenticator;
  private final TotpProperties properties;

  @Override
  public TotpCredentials generateCredentials(String accountName) {
    GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
    String secret = key.getKey();
    String url = buildOtpAuthUrl(accountName, secret);
    return new TotpCredentials(secret, url);
  }

  @Override
  public boolean verifyCode(String secret, int code) {
    return googleAuthenticator.authorize(secret, code);
  }

  @Override
  public String getOtpAuthUrl(String accountName, String secret) {
    return buildOtpAuthUrl(accountName, secret);
  }

  private String buildOtpAuthUrl(String accountName, String secret) {
    return String.format(
        "otpauth://totp/%s:%s?secret=%s&issuer=%s",
        properties.issuer(),
        accountName,
        secret,
        properties.issuer()
    );
  }
}
