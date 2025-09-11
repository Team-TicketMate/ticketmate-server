package com.ticketmate.backend.api.application.schema;

import com.ticketmate.backend.ai.infrastructure.entity.Embedding;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail;
import com.ticketmate.backend.applicationform.infrastructure.entity.HopeArea;
import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import com.ticketmate.backend.concert.infrastructure.entity.Concert;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertAgentAvailability;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import com.ticketmate.backend.concert.infrastructure.entity.TicketOpenDate;
import com.ticketmate.backend.concerthall.infrastructure.entity.ConcertHall;
import com.ticketmate.backend.member.infrastructure.entity.AgentPerformanceSummary;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.entity.MemberFollow;
import com.ticketmate.backend.portfolio.infrastructure.entity.Portfolio;
import com.ticketmate.backend.portfolio.infrastructure.entity.PortfolioImg;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;

import com.ticketmate.backend.report.infrastructure.entity.Report;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

public final class HibernateDdlGenerator {

  // ======= Public API =======
  /** CI/로컬 어디서든 호출할 수 있는 진입점 */
  public static Path generate(Path explicitOut) throws Exception {
    log("Hibernate 메타데이터를 기반으로 DDL 파일을 생성합니다. (스프링 미기동)");
    StandardServiceRegistry registry = null;
    try {
      registry = buildRegistry();
      MetadataSources sources = new MetadataSources(registry);
      registerEntities(sources);
      Metadata metadata = sources.buildMetadata();

      Path out = (explicitOut != null) ? explicitOut : resolveOutputPath();
      ensureParentDir(out);

      exportDdl(metadata, out);
      Path stable = writeStableCopy(out); // 항상 고정 이름으로도 복사 → CI에서 참조
      log("DDL 생성 완료 ✅ 파일: " + stable.toAbsolutePath());

      return stable; // 고정 경로 파일 반환
    } finally {
      if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
    }
  }

  /** 선택: 단독 실행도 가능 (CI나 로컬에서 편하게) */
  public static void main(String[] args) throws Exception {
    Path override = Optional
        .ofNullable(System.getProperty("hibernate.ddl.out"))
        .map(Paths::get)
        .orElse(null);
    Path result = generate(override);
    log("생성 완료. 경로: " + result.toAbsolutePath());
  }

  // ======= Internals =======

  /** Hibernate ServiceRegistry 구성 (방언 등 최소 설정만) */
  private static StandardServiceRegistry buildRegistry() {
    log("Hibernate Registry 생성 중...");
    return new StandardServiceRegistryBuilder()
        .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
        .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")   // ✅ JDBC 메타데이터 접근 금지
        .applySetting("hibernate.temp.use_jdbc_metadata_defaults", "false")   // ✅ 구버전 호환 옵션
        .build();
  }

  /** 엔티티 등록 (필요시 스캐닝으로 교체 가능) */
  private static void registerEntities(MetadataSources sources) {
    log("엔티티 등록 중...");
    // === 여기에 엔티티 전부 추가 ===
    sources.addAnnotatedClass(Member.class);
    sources.addAnnotatedClass(MemberFollow.class);
    sources.addAnnotatedClass(AgentPerformanceSummary.class);
    sources.addAnnotatedClass(Concert.class);
    sources.addAnnotatedClass(ConcertDate.class);
    sources.addAnnotatedClass(TicketOpenDate.class);
    sources.addAnnotatedClass(ConcertAgentAvailability.class);
    sources.addAnnotatedClass(ConcertHall.class);
    sources.addAnnotatedClass(Embedding.class);
    sources.addAnnotatedClass(ApplicationForm.class);
    sources.addAnnotatedClass(ApplicationFormDetail.class);
    sources.addAnnotatedClass(HopeArea.class);
    sources.addAnnotatedClass(RejectionReason.class);
    sources.addAnnotatedClass(Portfolio.class);
    sources.addAnnotatedClass(PortfolioImg.class);
    sources.addAnnotatedClass(Report.class);
    // ===============================
  }

  /** 출력 파일 경로 결정: 고유 파일 + 안정 파일(고정 이름) 모두 관리 */
  private static Path resolveOutputPath() {
    Path moduleRoot = resolveModuleRoot();
    Path genDir = moduleRoot.resolve("build").resolve("generated");
    String runTag = buildRunTag();
    String fname = "hibernate-ddl_" + runTag + ".sql";
    Path unique = genDir.resolve(fname);
    log("출력 파일명(고유): " + fname);
    return unique;
  }

  /** 모놀리포(root)와 모듈 단독 실행 모두 안전하게 처리 */
  private static Path resolveModuleRoot() {
    Path cwd = Paths.get("").toAbsolutePath();
    if (Files.isDirectory(cwd.resolve("ticketmate-api"))) {
      // 루트에서 실행되는 경우
      return cwd.resolve("ticketmate-api");
    }
    // 모듈 디렉터리에서 실행되는 경우
    return cwd;
  }

  /** 실제 DDL 내보내기 (SchemaExport 사용 → ExecutionOptions 불필요) */
  private static void exportDdl(Metadata metadata, Path outFile) {
    log("DDL 파일 생성 중...");
    SchemaExport export = new SchemaExport();
    export.setDelimiter(";");
    export.setFormat(false);
    export.setHaltOnError(true);
    export.setOutputFile(outFile.toString());

    // createOnly: 실제 DB는 건드리지 않고 스크립트만 생성
    export.createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
  }

  /** 항상 고정 이름 파일도 유지 → CI가 이 파일만 보면 됨 */
  private static Path writeStableCopy(Path uniqueFile) throws Exception {
    Path moduleRoot = resolveModuleRoot();
    Path genDir = moduleRoot.resolve("build").resolve("generated");
    Path stable = genDir.resolve("hibernate-ddl.sql");
    ensureParentDir(stable);
    Files.copy(uniqueFile, stable, StandardCopyOption.REPLACE_EXISTING);
    log("안정 파일(고정 이름)로 복사: " + stable.getFileName());
    return stable;
  }

  /** 부모 디렉터리 보장 */
  private static void ensureParentDir(Path p) {
    try {
      Files.createDirectories(p.getParent());
    } catch (Exception e) {
      throw new RuntimeException("출력 디렉터리 생성 실패: " + p.getParent(), e);
    }
  }

  /** 실행 태그: 타임스탬프 + Git SHA(있으면) */
  private static String buildRunTag() {
    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String sha = Optional.ofNullable(System.getenv("GITHUB_SHA"))
        .map(s -> s.substring(0, Math.min(8, s.length())))
        .orElse("local");
    return ts + "_" + sha;
  }

  private static void log(String msg) { System.out.println("[DDL] " + msg); }
}