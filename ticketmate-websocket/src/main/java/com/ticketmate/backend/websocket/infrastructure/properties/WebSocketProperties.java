package com.ticketmate.backend.websocket.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "websocket")
public record WebSocketProperties(
    @NotBlank String host,
    @NotBlank String username,
    @NotBlank String password,
    int stompPort
) {

}
