package com.ticketmate.backend.common.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

  /**
   * 시스템 기본 타임존 (Asia/Seoul)의 Clock을 빈으로 등록
   */
  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of("Asia/Seoul"));
  }
}
