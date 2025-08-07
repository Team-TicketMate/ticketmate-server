package com.ticketmate.backend.totp.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.totp")
public record TotpProperties(
    @NotBlank String issuer
) {

}
