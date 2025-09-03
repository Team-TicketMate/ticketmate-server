package com.ticketmate.backend.api;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;

/**
 * 스키마 검증을 위한 "얇은" 컨텍스트.
 * - DataSource + Flyway + JPA만 자동설정
 * - 보안/웹/메시징/몽고/레디스/웹소켓 등은 제외
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    // 웹/보안
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class,
    ReactiveOAuth2ClientAutoConfiguration.class,
    WebFluxAutoConfiguration.class,
    WebSocketServletAutoConfiguration.class,

    // 메시징/캐시
    RabbitAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,

    // MongoDB
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class,

    // JPA 레포지토리 스캔은 굳이 안 해도 됨(속도↑)
    JpaRepositoriesAutoConfiguration.class
})
@EntityScan(basePackages = "com.ticketmate.backend") // 모든 모듈의 @Entity만 스캔
public class SchemaVerifyApplication {

}
