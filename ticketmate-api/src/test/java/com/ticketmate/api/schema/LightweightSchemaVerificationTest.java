package com.ticketmate.api.schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.ContributableDatabaseObject;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.ScriptSourceInput;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.SourceDescriptor;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.junit.jupiter.api.Test;

/**
 * JPA 엔티티만을 사용한 경량 스키마 검증
 * - Spring Context 로드 없이 Hibernate만 사용하여 DDL 생성
 * - Flyway 마이그레이션과 비교
 */
public class LightweightSchemaVerificationTest {

  @Test
  void verifySchemaCompatibility() throws IOException {
    // 1. JPA 엔티티들로부터 DDL 생성
    String hibernateDdl = generateHibernateDdl();

    // 2. Flyway 마이그레이션 파일 읽기
    String flywayDdl = readFlywayMigrations();

    // 3. DDL 정규화 및 비교
    String normalizedHibernateDdl = normalizeDdl(hibernateDdl);
    String normalizedFlywayDdl = normalizeDdl(flywayDdl);

    // 4. 비교 결과 출력
    System.out.println("🔍 스키마 검증을 시작합니다...");
    System.out.println("📝 Hibernate DDL 라인 수: " + normalizedHibernateDdl.split("\n").length);
    System.out.println("📝 Flyway DDL 라인 수: " + normalizedFlywayDdl.split("\n").length);

    if (normalizedHibernateDdl.equals(normalizedFlywayDdl)) {
      System.out.println("✅ DDL 스키마가 일치합니다!");
    } else {
      System.out.println("❌ DDL 스키마에 차이점이 발견되었습니다:");
      showDifferences(normalizedHibernateDdl, normalizedFlywayDdl);
      throw new AssertionError("DDL 스키마 불일치 - Hibernate entities don't match Flyway migrations");
    }
  }

  private String generateHibernateDdl() throws IOException {
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        .applySetting("hibernate.hbm2ddl.auto", "create")
        .applySetting("hibernate.hbm2ddl.delimiter", ";")
        .applySetting("jakarta.persistence.schema-generation.database.action", "none")
        .applySetting("jakarta.persistence.schema-generation.scripts.action", "create")
        .applySetting("jakarta.persistence.schema-generation.create-source", "metadata")
        .build();

    try {
      MetadataSources sources = new MetadataSources(registry);

      // 모든 JPA 엔티티 클래스 추가
      sources.addAnnotatedClass(com.ticketmate.backend.member.infrastructure.entity.Member.class);
      sources.addAnnotatedClass(com.ticketmate.backend.member.infrastructure.entity.MemberFollow.class);
      sources.addAnnotatedClass(com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary.class);
      sources.addAnnotatedClass(com.ticketmate.backend.concert.infrastructure.entity.Concert.class);
      sources.addAnnotatedClass(com.ticketmate.backend.concert.infrastructure.entity.ConcertDate.class);
      sources.addAnnotatedClass(com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate.class);
      sources.addAnnotatedClass(com.ticketmate.backend.concert.infrastructure.entity.ConcertAgentAvailability.class);
      sources.addAnnotatedClass(com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall.class);
      sources.addAnnotatedClass(com.ticketmate.backend.ai.infrastructure.entity.Embedding.class);
      sources.addAnnotatedClass(com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm.class);
      sources.addAnnotatedClass(com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail.class);
      sources.addAnnotatedClass(com.ticketmate.backend.applicationform.infrastructure.entity.HopeArea.class);
      sources.addAnnotatedClass(com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason.class);
      sources.addAnnotatedClass(com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio.class);
      sources.addAnnotatedClass(com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg.class);

      // 메타데이터 빌드
      Metadata metadata = sources.buildMetadata();

      // 임시 파일 경로 설정
      Path tempDir = Files.createTempDirectory("hibernate-ddl");
      Path ddlFile = tempDir.resolve("schema.sql");

      // JPA 표준 방식으로 DDL 생성
      registry.getService(SchemaManagementTool.class)
          .getSchemaCreator(null)
          .doCreation(
              metadata,
              new ExecutionOptions() {
                @Override
                public Map<String, Object> getConfigurationValues() {
                  return Map.of();
                }

                @Override
                public boolean shouldManageNamespaces() {
                  return false;
                }

                @Override
                public ExceptionHandler getExceptionHandler() {
                  return org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl.INSTANCE;
                }
              },
              new ContributableMatcher() {
                @Override
                public boolean matches(ContributableDatabaseObject contributed) {
                  return false;
                }
              },
              new SourceDescriptor() {
                @Override
                public org.hibernate.tool.schema.SourceType getSourceType() {
                  return org.hibernate.tool.schema.SourceType.METADATA;
                }

                @Override
                public ScriptSourceInput getScriptSourceInput() {
                  return null;
                }
              },
              new TargetDescriptor() {
                @Override
                public java.util.EnumSet<org.hibernate.tool.schema.TargetType> getTargetTypes() {
                  return java.util.EnumSet.of(org.hibernate.tool.schema.TargetType.SCRIPT);
                }

                @Override
                public ScriptTargetOutput getScriptTargetOutput() {
                  try {
                    return new org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile(ddlFile.toFile(), "UTF-8");
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                }
              }
          );

      String ddlContent = Files.readString(ddlFile);

      // 정리
      Files.deleteIfExists(ddlFile);
      Files.deleteIfExists(tempDir);

      // 파일로도 저장 (디버깅용)
      Path buildDir = Paths.get(System.getProperty("user.dir"), "ticketmate-api", "build", "generated");
      Files.createDirectories(buildDir);
      Path outputPath = buildDir.resolve("hibernate-ddl-direct.sql");
      Files.writeString(outputPath, ddlContent);

      System.out.println("📝 생성된 Hibernate DDL:");
      System.out.println(ddlContent.substring(0, Math.min(500, ddlContent.length())) + "...");

      return ddlContent;

    } catch (Exception e) {
      throw new IOException("Hibernate DDL 생성 중 오류 발생: " + e.getMessage(), e);
    } finally {
      StandardServiceRegistryBuilder.destroy(registry);
    }
  }

  private String readFlywayMigrations() throws IOException {
    // 절대 경로로 마이그레이션 디렉토리 설정
    Path migrationDir = Paths.get(System.getProperty("user.dir"), "ticketmate-api", "src", "main", "resources", "db", "migration");

    if (!Files.exists(migrationDir)) {
      throw new RuntimeException("Flyway 마이그레이션 디렉토리를 찾을 수 없습니다: " + migrationDir);
    }

    List<Path> migrationFiles = Files.walk(migrationDir)
        .filter(path -> path.toString().endsWith(".sql"))
        .sorted()
        .collect(Collectors.toList());

    if (migrationFiles.isEmpty()) {
      throw new RuntimeException("Flyway 마이그레이션 파일이 없습니다.");
    }

    StringBuilder allMigrations = new StringBuilder();
    for (Path file : migrationFiles) {
      System.out.println("📄 읽는 마이그레이션 파일: " + file.getFileName());
      allMigrations.append(Files.readString(file)).append("\n");
    }

    return allMigrations.toString();
  }

  private String normalizeDdl(String ddl) {
    return ddl.lines()
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .filter(line -> !line.startsWith("--"))
        .filter(line -> !line.startsWith("/*"))
        .map(line -> line.replaceAll("\\s+", " "))
        .map(line -> line.toLowerCase())
        .sorted()
        .collect(Collectors.joining("\n"));
  }

  private void showDifferences(String hibernateDdl, String flywayDdl) {
    String[] hibernateLines = hibernateDdl.split("\n");
    String[] flywayLines = flywayDdl.split("\n");

    System.out.println("\n=== Hibernate DDL (처음 10줄) ===");
    for (int i = 0; i < Math.min(10, hibernateLines.length); i++) {
      System.out.println(hibernateLines[i]);
    }

    System.out.println("\n=== Flyway DDL (처음 10줄) ===");
    for (int i = 0; i < Math.min(10, flywayLines.length); i++) {
      System.out.println(flywayLines[i]);
    }

    System.out.println("\n상세한 차이점은 빌드 아티팩트에서 확인할 수 있습니다.");
  }
}
