package com.ticketmate.backend.redis.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "redisson")
public record RedissonProperties(
    int waitTime,
    int leaseTime
) {

}
