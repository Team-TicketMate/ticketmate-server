package com.ticketmate.backend.sms.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "cool-sms")
public class CoolSmsProperties {

  @NotBlank
  private final String apiKey;

  @NotBlank
  private final String apiSecret;

  @NotBlank
  private final String baseUrl;

  @NotBlank
  private final String from;
}
