package com.ticketmate.backend.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {

  @NotBlank
  private final String host;

  @NotBlank
  private final String username;

  @NotBlank
  private final String password;

  private final int stompPort;
}
