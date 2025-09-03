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
    // JPA 기반 모듈들만 추려서 스캔 (예시: common, member, concert, applicationform 등)
    basePackages = {
        "com.ticketmate.backend.member",
        "com.ticketmate.backend.concert",
        "com.ticketmate.backend.applicationform",
        "com.ticketmate.backend.concerthall",
        "com.ticketmate.backend.portfolio",
    },
    // API 컨트롤러/채팅/메시징 등 외부 인프라 의존 강한 패키지는 통째로 제외
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.ticketmate\\.backend\\.(api\\.application\\.controller|admin|api|auth|chat|common|messaging|mock|notification|querydsl|redis|search|sms|storage|totp|websocket).*"
    )
)
public class SchemaVerifyApplication {

}
