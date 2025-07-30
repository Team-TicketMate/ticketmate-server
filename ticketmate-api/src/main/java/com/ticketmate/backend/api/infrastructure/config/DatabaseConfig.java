package com.ticketmate.backend.api.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
    "com.ticketmate.backend.*",
})
@EntityScan(basePackages = {
    "com.ticketmate.backend.*"
})
public class DatabaseConfig {

}
