package com.ticketmate.backend.sms.infrastructure.config;

import com.ticketmate.backend.sms.infrastructure.properties.CoolSmsProperties;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CoolSmsProperties.class)
public class CoolSmsConfig {

  private final CoolSmsProperties properties;

  @Bean
  public DefaultMessageService coolSmsMessageService() {
    return NurigoApp.INSTANCE.initialize(
        properties.getApiKey(), properties.getApiSecret(), properties.getBaseUrl());
  }
}
