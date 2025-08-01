package com.ticketmate.backend.auth.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    @NotBlank String secretKey,
    long accessExpMillis,
    long refreshExpMillis,
    @NotBlank String issuer
) {

}
