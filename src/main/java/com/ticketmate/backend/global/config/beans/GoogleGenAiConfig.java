package com.ticketmate.backend.global.config.beans;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.genai.Client;
import com.ticketmate.backend.global.config.properties.GoogleGenAiProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class GoogleGenAiConfig {

  @Bean
  public Client genAiClient(GoogleGenAiProperties properties) throws IOException {
    // JSON 파일 로드
    ClassPathResource classPathResource = new ClassPathResource(properties.getCredentialsFile());
    try (InputStream inputStream = classPathResource.getInputStream()) {

      // 서비스 계정 로드
      GoogleCredentials googleCredentials =
          ServiceAccountCredentials.fromStream(inputStream)
              .createScoped(List.of(properties.getCloudPlatformUrl()));

      return Client.builder()
          .vertexAI(properties.isVertexAiEnabled())
          .project(properties.getProjectId())
          .location(properties.getLocation())
          .credentials(googleCredentials)
          .build();
    }
  }
}
