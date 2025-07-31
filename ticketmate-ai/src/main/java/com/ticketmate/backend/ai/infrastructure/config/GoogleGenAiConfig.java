package com.ticketmate.backend.ai.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.genai.Client;
import com.ticketmate.backend.ai.infrastructure.properties.GoogleGenAiProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableConfigurationProperties(GoogleGenAiProperties.class)
public class GoogleGenAiConfig {

  @Bean
  public Client genAiClient(GoogleGenAiProperties properties) throws IOException {
    // JSON 파일 로드
    ClassPathResource classPathResource = new ClassPathResource(properties.credentialsFile());
    try (InputStream inputStream = classPathResource.getInputStream()) {

      // 서비스 계정 로드
      GoogleCredentials googleCredentials =
          ServiceAccountCredentials.fromStream(inputStream)
              .createScoped(List.of(properties.cloudPlatformUrl()));

      return Client.builder()
          .vertexAI(properties.vertexAiEnabled())
          .project(properties.projectId())
          .location(properties.location())
          .credentials(googleCredentials)
          .build();
    }
  }
}
