package com.ticketmate.backend.global.config;

import lombok.Getter;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CoolSmsConfig {

  @Value("${cool-sms.api-key}")
  private String apiKey;

  @Value("${cool-sms.api-secret}")
  private String apiSecret;

  @Value("${cool-sms.base-url}")
  private String baseUrl;

  @Value("${cool-sms.from}")
  private String from;

  @Bean
  public DefaultMessageService coolSmsMessageService() {
    return NurigoApp.INSTANCE.initialize(apiKey, apiSecret, baseUrl);
  }
}
