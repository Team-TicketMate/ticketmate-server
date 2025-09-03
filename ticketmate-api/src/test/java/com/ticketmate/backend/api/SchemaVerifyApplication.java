package com.ticketmate.backend.api;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class,
    ReactiveOAuth2ClientAutoConfiguration.class,
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@ComponentScan(
    basePackages = {
        "com.ticketmate.backend" // 전체 모듈 루트
    },
    excludeFilters = {
        // 애플리케이션 레이어(services, facades 등) 제외 → 인프라(엔티티/리포지토리/설정)만 띄움
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.ticketmate\\.backend\\..*\\.application\\..*"),
        // 웹/WebSocket 등도 혹시 있을 수 있는 구성요소 제외
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.ticketmate\\.backend\\..*\\.repository\\..*")
    }
)
public class SchemaVerifyApplication {

}
