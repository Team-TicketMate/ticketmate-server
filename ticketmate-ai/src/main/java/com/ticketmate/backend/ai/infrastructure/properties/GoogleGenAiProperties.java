package com.ticketmate.backend.ai.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "google.genai")
public record GoogleGenAiProperties(
    @NotBlank String projectId,
    @NotBlank String location,
    @NotBlank String credentialsFile,
    boolean vertexAiEnabled,
    @NotBlank String model,
    @NotBlank String cloudPlatformUrl
) {

}
