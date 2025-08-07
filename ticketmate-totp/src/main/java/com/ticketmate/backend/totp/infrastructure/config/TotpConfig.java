package com.ticketmate.backend.totp.infrastructure.config;

import com.ticketmate.backend.totp.infrastructure.properties.TotpProperties;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TotpProperties.class)
public class TotpConfig {

  @Bean
  public GoogleAuthenticator googleAuthenticator() {
    GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig();
    return new GoogleAuthenticator(config);
  }

  @Bean
  public GoogleAuthenticatorConfig googleAuthenticatorConfig() {
    return new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
        .setTimeStepSizeInMillis(30_000)
        .setWindowSize(1)
        .setCodeDigits(6)
        .build();
  }
}
