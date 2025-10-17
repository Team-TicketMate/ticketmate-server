package com.ticketmate.backend.crypto.infrastructure.config;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.crypto.infrastructure.properties.AesGcmProperties;
import com.ticketmate.backend.crypto.infrastructure.provider.AesGcmProvider;
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

  /** 키 초기화 (정적) */
  @Bean(name = "aesGcmInit")
  public InitializingBean aesGcmInit() {
    return () -> {
      // (선택) 길이 검증
      byte[] raw = java.util.Base64.getDecoder().decode(properties.secretKey());
      if (raw.length != 32) throw new CustomException(ErrorCode.AES_KEY_LENGTH_INVALID);

      // 정적 프로바이더 초기화
      AesGcmProvider.initFromSecretKey(new javax.crypto.spec.SecretKeySpec(raw, "AES"));
    };
  }

  /** JPA(SessionFactory) 생성 전에 키 초기화 보장 */
  @Bean
  public static org.springframework.beans.factory.config.BeanFactoryPostProcessor forceInitOrder() {
    return bf -> {
      if (bf.containsBeanDefinition("entityManagerFactory")) {
        bf.getBeanDefinition("entityManagerFactory").setDependsOn("aesGcmInit");
      }
    };
  }
}