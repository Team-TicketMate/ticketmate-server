-- 모든 PG 테이블에 soft-delete 컬럼 추가
-- deleted BOOLEAN NOT NULL DEFAULT FALSE
-- deleted_date TIMESTAMPTZ(0) NULL
-- flyway_schema_history 등 마이그레이션 관리 테이블 제외

DO
$$
  DECLARE
    r RECORD;
  BEGIN
    FOR r IN
      SELECT tablename
      FROM pg_tables
      WHERE schemaname = CURRENT_SCHEMA()
        AND tablename <> 'flyway_schema_history'
      LOOP
        EXECUTE FORMAT(
            'ALTER TABLE %I ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;',
            r.tablename
                );

        EXECUTE FORMAT(
            'ALTER TABLE %I ADD COLUMN IF NOT EXISTS deleted_date TIMESTAMPTZ(0);',
            r.tablename
                );
      END LOOP;
  END;
$$;