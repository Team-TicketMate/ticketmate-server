package com.ticketmate.backend.storage.infrastructure.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ticketmate.backend.storage.infrastructure.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

  private final S3Properties properties;

  @Bean
  public AmazonS3 amazonS3() {
    AWSCredentials credentials = new BasicAWSCredentials(
        properties.credentials().accessKey(),
        properties.credentials().secretKey()
    );

    return AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(properties.region().staticRegion())
        .build();
  }
}