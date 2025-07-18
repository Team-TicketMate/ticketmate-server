package com.ticketmate.backend.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMqProperties {

  @NotBlank
  private final String host;

  @NotBlank
  private final String virtualHost;

  @NotBlank
  private final String username;

  @NotBlank
  private final String password;

  private final int port;

  private final int stompPort;

}
