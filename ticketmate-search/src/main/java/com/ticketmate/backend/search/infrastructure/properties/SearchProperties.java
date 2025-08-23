package com.ticketmate.backend.search.infrastructure.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "search")
public record SearchProperties (Recent recent) {
    public record Recent(@Min(1) int maxSize, @Min(1) long ttlDays) { }
}
