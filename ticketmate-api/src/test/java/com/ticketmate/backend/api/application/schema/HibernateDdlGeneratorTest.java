package com.ticketmate.backend.api.application.schema;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class HibernateDdlGeneratorTest {

  @Test
  void generatesHibernateDdlWithoutSpringContext() throws Exception {
    System.out.println("🔧 Hibernate 메타데이터만으로 DDL 스냅샷을 생성합니다. (애플리케이션 미기동)");
    // 스프링 기동 없이 순수 Hibernate로 DDL 생성
    Path out = HibernateDdlGenerator.generate(null);
    // CI에서 비교에 쓰는 고정 이름 파일 확인
    System.out.println("📝 생성된 DDL 파일: " + out.toAbsolutePath());
    Path stable = Paths.get("ticketmate-api","build","generated","hibernate-ddl.sql");
    assertTrue(Files.exists(out), "DDL 파일이 실제로 생성되어야 합니다.");
    assertTrue(Files.size(out) > 0, "DDL 파일은 비어있지 않아야 합니다.");
    assertTrue(Files.exists(stable));
    System.out.println("✅ 생성 완료. 이후 단계에서 Flyway 덤프와 비교합니다.");
  }
}