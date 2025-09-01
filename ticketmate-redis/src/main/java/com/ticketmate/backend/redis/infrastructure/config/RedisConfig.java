package com.ticketmate.backend.redis.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory factory,
      @Qualifier("redisJsonSerializer") GenericJackson2JsonRedisSerializer serializer
  ) {
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    RedisTemplate<String, Object> template = new RedisTemplate<>();

    template.setConnectionFactory(factory);
    // 직렬화 설정
    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);
    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);
    template.afterPropertiesSet();
    return template;
  }
}
