package com.ticketmate.backend.auth.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.app.redirect-uri")
public record OAuth2Properties(
    String home,
    String phoneVerify,
    String setProfile
) {

}
