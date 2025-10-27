package com.ticketmate.backend.auth.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.security.app.redirect-uri")
public record OAuth2Properties(
    @NotBlank
    String home,
    @NotBlank
    String phoneVerify,
    @NotBlank
    String setProfile
) {

}
