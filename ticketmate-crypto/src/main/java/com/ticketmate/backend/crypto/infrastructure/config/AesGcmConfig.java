package com.ticketmate.backend.crypto.infrastructure.config;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.crypto.infrastructure.properties.AesGcmProperties;
import com.ticketmate.backend.crypto.infrastructure.provider.AesGcmProvider;
import java.util.Arrays;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AesGcmProperties.class)
public class AesGcmConfig {

  private final AesGcmProperties properties;

  /**
   * 키 초기화 (정적)
   */
  @Bean(name = "aesGcmInit")
  public InitializingBean aesGcmInit() {
    return () -> {
      // (선택) 길이 검증
      byte[] raw = Base64.getDecoder().decode(properties.secretKey());
      if (raw.length != 32) {
        throw new CustomException(ErrorCode.AES_KEY_LENGTH_INVALID);
      }

      try {
        // 정적 프로바이더 초기화
        AesGcmProvider.initFromSecretKey(new javax.crypto.spec.SecretKeySpec(raw, "AES"));
      } finally {
        Arrays.fill(raw, (byte) 0);
      }
    };
  }
}