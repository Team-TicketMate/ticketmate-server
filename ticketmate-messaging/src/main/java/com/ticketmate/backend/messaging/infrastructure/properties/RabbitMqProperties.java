package com.ticketmate.backend.messaging.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "messaging.rabbitmq")
public record RabbitMqProperties(
    @NotBlank String host,
    @NotBlank String virtualHost,
    @NotBlank String username,
    @NotBlank String password,
    int port,
    int stompPort
) {

}
