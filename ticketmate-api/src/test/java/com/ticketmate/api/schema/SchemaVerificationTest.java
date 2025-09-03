package com.ticketmate.api.schema;

import com.ticketmate.backend.api.BackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 스키마 검증 테스트
 * - Flyway 마이그레이션 실행
 * - JPA DDL 검증 (hibernate.ddl-auto=validate)
 * - DDL 스냅샷 생성
 */
@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("schema-verify")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "logging.level.org.flywaydb=DEBUG",
    "logging.level.org.hibernate.SQL=DEBUG"
})
public class SchemaVerificationTest {

    @Test
    void verifySchemaCompatibility() {
        // 이 테스트는 단순히 Spring Context가 정상적으로 로드되는지 확인합니다.
        // Flyway가 마이그레이션을 실행하고, Hibernate가 validate 모드로 스키마를 검증합니다.
        // application-schema-verify.yml에서 DDL 스냅샷 생성도 함께 이루어집니다.

        // Context가 성공적으로 로드되면 스키마가 일치한다는 의미입니다.
        System.out.println("✅ Schema verification passed - Flyway migrations match JPA entities");
    }
}
