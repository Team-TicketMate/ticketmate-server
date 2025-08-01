package com.ticketmate.backend.notification.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
    @NotBlank String serviceAccountKeyPath
) {

}
