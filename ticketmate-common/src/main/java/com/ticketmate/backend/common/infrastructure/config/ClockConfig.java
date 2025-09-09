package com.ticketmate.backend.common.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ClockConfig {

  /**
   * 기본 UTC (Instant 측정 / 엔티티 저장용)
   */
  @Bean
  @Primary
  public Clock clock() {
    return Clock.systemUTC();
  }

  /**
   * KST ZoneId
   */
  @Bean
  public ZoneId zoneId() {
    return ZoneId.of("Asia/Seoul");
  }
}
