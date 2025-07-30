package com.ticketmate.backend.storage.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Properties {

  private final S3 s3;

  private final Credentials credentials;

  private final Region region;

  private final Stack stack;

  @Getter
  @AllArgsConstructor
  public static class S3 {

    @NotBlank
    private final String bucket;

    @NotBlank
    private final String domain;

    private final Path path;
  }

  @Getter
  @AllArgsConstructor
  public static class Path {

    @NotBlank
    private final String member;

    @NotBlank
    private final String concertHall;

    @NotBlank
    private final String concert;

    @NotBlank
    private final String portfolio;

    @NotBlank
    private final String chat;
  }

  @Getter
  @AllArgsConstructor
  public static class Credentials {

    @NotBlank
    private final String accessKey;

    @NotBlank
    private final String secretKey;

  }

  @Getter
  @AllArgsConstructor
  public static class Region {

    @NotBlank
    private final String staticRegion;

    private final boolean auto;

  }

  @Getter
  @AllArgsConstructor
  public static class Stack {

    private final boolean auto;

  }
}
