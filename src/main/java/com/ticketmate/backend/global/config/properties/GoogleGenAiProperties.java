package com.ticketmate.backend.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "google.genai")
public class GoogleGenAiProperties {

  @NotBlank
  private final String projectId;

  @NotBlank
  private final String location;

  @NotBlank
  private final String credentialsFile;

  private final boolean vertexAiEnabled;

  @NotBlank
  private final String model;

  @NotBlank
  private final String cloudPlatformUrl;
}
