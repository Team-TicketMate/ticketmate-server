package com.ticketmate.backend.redis.infrastructure.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

  @Bean
  public RedisCacheManager redisCacheManager(
      RedisConnectionFactory connectionFactory,
      @Qualifier("redisJsonSerializer") GenericJackson2JsonRedisSerializer serializer
  ) {
    RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
        .disableCachingNullValues()
        .entryTtl(Duration.ofMinutes(30));

    // 캐시별 TTL 설정
    Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
    // Embedding 캐시는 TTL 7일 설정
    configurationMap.put("embeddings", cacheConfiguration.entryTtl(Duration.ofDays(7)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfiguration)
        .withInitialCacheConfigurations(configurationMap)
        .transactionAware()
        .build();
  }
}
