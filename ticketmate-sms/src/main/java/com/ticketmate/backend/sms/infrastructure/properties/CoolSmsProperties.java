package com.ticketmate.backend.sms.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cool-sms")
public record CoolSmsProperties(
    @NotBlank String apiKey,
    @NotBlank String apiSecret,
    @NotBlank String baseUrl,
    @NotBlank String from
) {

}
