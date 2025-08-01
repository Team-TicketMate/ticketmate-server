package com.ticketmate.backend.mock.infrastructure.config;

import me.suhsaechan.suhnicknamegenerator.core.SuhRandomKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SuhRandomKitConfig {

  @Bean
  public SuhRandomKit suhRandomKit() {
    return SuhRandomKit.builder()
        .locale("ko")
        .numberLength(4)
        .uuidLength(4)
        .build();
  }
}
