package com.ticketmate.backend.util.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FireBaseConfig {
    @PostConstruct
    public void init() {
        try {
            // resources 디렉토리에 있는 서비스 계정 키 파일 로드
            InputStream serviceAccount = getClass()
                    .getResourceAsStream("/ticketmate-firebase-key.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 이미 FirebaseApp이 초기화되어 있지 않았다면 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.debug("FireBase 설정 초기화 완료");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
