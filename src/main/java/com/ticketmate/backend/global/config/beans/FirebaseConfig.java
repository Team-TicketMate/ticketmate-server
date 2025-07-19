package com.ticketmate.backend.global.config.beans;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.ticketmate.backend.global.config.properties.FirebaseProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

  private final FirebaseProperties properties;

  @PostConstruct
  public void init() throws IOException {
    try {
      // resources 디렉토리에 있는 서비스 계정 키 파일 로드
      InputStream serviceAccount = getClass()
          .getResourceAsStream(properties.getServiceAccountKeyPath());

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();

      // 이미 FirebaseApp이 초기화되어 있지 않았다면 초기화
      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
        log.debug("FireBase 설정 초기화 완료");
      }
    } catch (IOException e) {
      log.error("Firebase 초기화 중 오류가 발생했습니다: {}", e.getMessage());
      throw e;
    }
  }
}
