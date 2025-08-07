package com.ticketmate.backend.auth.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.admin")
public record AuthProperties(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String name,
    String totpSecret
) {

}
