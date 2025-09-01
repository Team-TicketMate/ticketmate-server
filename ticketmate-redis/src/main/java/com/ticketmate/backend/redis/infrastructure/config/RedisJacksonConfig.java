package com.ticketmate.backend.redis.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisJacksonConfig {

  @Bean("redisObjectMapper")
  public ObjectMapper redisObjectMapper() {
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("com.ticketmate.backend") // 도메인
        .allowIfSubType("java.util.") // 컬렉션
        .build();
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);
  }

  @Bean("redisJsonSerializer")
  public GenericJackson2JsonRedisSerializer redisSerializer(
      @Qualifier("redisObjectMapper") ObjectMapper objectMapper
  ) {
    return new GenericJackson2JsonRedisSerializer(objectMapper);
  }
}
