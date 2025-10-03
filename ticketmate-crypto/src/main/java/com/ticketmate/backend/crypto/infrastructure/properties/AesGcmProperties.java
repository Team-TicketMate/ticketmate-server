package com.ticketmate.backend.crypto.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aes-gcm")
public record AesGcmProperties (
  @NotBlank String secretKey
) {
}
