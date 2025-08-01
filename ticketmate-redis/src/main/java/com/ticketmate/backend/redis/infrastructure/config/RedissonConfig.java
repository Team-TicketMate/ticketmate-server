package com.ticketmate.backend.redis.infrastructure.config;

import com.ticketmate.backend.redis.infrastructure.properties.RedissonProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {

  private final RedisProperties redisProperties;

  @Bean(destroyMethod = "shutdown")
  public RedissonClient redissonClient() {
    String address = String.format("redis://%s:%d",
        redisProperties.getHost(),
        redisProperties.getPort());

    Config config = new Config();
    config.useSingleServer()
        .setAddress(address)
        .setPassword(redisProperties.getPassword());
    return Redisson.create(config);
  }
}
