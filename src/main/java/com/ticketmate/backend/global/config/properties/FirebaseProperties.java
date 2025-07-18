package com.ticketmate.backend.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

  @NotBlank
  private final String serviceAccountKeyPath;
}
