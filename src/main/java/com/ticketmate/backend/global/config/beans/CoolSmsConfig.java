package com.ticketmate.backend.global.config.beans;

import com.ticketmate.backend.global.config.properties.CoolSmsProperties;
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
