package com.ticketmate.backend.common.infrastructure.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonTimeConfig {

  private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
  private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Seoul");

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return (Jackson2ObjectMapperBuilder builder) -> {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);

      JavaTimeModule javaTimeModule = new JavaTimeModule();
      javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
      javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

      builder.modules(javaTimeModule);
      builder.timeZone(TIME_ZONE);
      builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    };
  }

}
