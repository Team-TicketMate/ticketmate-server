package com.ticketmate.backend.messaging.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "messaging.rabbitmq.chat")
public record ChatRabbitMqProperties(
    @NotBlank String queueName,
    @NotBlank String exchangeName,
    @NotBlank String unreadRoutingKey
) {

}
