package com.ticketmate.backend.api.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EntityScan(basePackages = "com.ticketmate.backend.*")
@EnableJpaRepositories(basePackages = "com.ticketmate.backend.*")
@EnableMongoRepositories(basePackages = "com.ticketmate.backend.*")
@EnableRedisRepositories(basePackages = "com.ticketmate.backend.*")
public class DatabaseScanConfig {

}
