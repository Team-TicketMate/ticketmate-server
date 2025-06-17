package com.ticketmate.backend.util.config;

import java.util.Locale;
import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FakerConfig {

  @Bean
  public Faker koFaker() {
    return new Faker(new Locale("ko", "KR"));
  }

  @Bean
  public Faker enFaker() {
    return new Faker(new Locale("en"));
  }
}
