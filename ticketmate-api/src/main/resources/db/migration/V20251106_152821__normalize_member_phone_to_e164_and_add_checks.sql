-- 1. 기존 member.phone "010-1234-5678" -> "+821012345678" E.164 형식으로의 변환
-- 2. E.164 한국 010 모바일 패턴 CHECK 제약 조건 추가
-- 3. 활성화 유니크 (deleted = false)

-- 기존 데이터 검증
-- "010-1234-5678" or "+821012345678" 형식이 아닌 경우 예외처리
DO
$$
  DECLARE
    v_count bigint;
  BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM member
    WHERE phone IS NOT NULL
      AND phone !~ '^(010-\d{4}-\d{4})$'
      AND phone !~ '^\+8210\d{8}$';

    IF v_count > 0 THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: phone column 내부에 (010-1234-5678) 또는 (+821012345678) 과 일치하지 않는 형식의 전화번호가 저장되어있습니다. 개수: %', v_count;
    END IF;
  END;
$$;

-- "010-1234-5678" 형식을 E.164로 매핑했을 때 활성(deleted = false) 행 기준 중복 검증
DO
$$
  DECLARE
    v_count bigint;
  BEGIN
    WITH candidates AS (
      SELECT member_id,
             deleted,
             phone,
             REGEXP_REPLACE(phone, '^010-(\d{4})-(\d{4})$', '+8210\1\2') AS new_e164
      FROM member
      WHERE phone ~ '^010-(\d{4})-(\d{4})$'
    )
    SELECT COUNT(*)
    INTO v_count
    FROM (
           -- 변환된 대상끼리 new_e164 가 겹치는지 검증 (활성 행 기준)
           SELECT new_e164
           FROM candidates
           WHERE deleted = FALSE
           GROUP BY new_e164
           HAVING COUNT(*) > 1
           UNION ALL

           -- 이미 new_164가 member.phone에 존재하는지 검증 (활성 행 기준)
           SELECT c.new_e164
           FROM candidates c
                  JOIN member m
                       ON m.phone = c.new_e164
           WHERE c.deleted = FALSE
             AND m.deleted = FALSE
         ) dup;
    IF v_count > 0 THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: 기존 전화번호 (010-1234-5678)를 E.164 형식으로 변환중 중복된 전화번호 데이터가 존재합니다.';
    END IF;
  END;
$$;

-- "010-1234-5678" -> "+821012345678" 일괄 변환
UPDATE member
SET phone = REGEXP_REPLACE(phone, '^010-(\d{4})-(\d{4})$', '+8210\1\2')
WHERE phone ~ '^010-(\d{4})-(\d{4})$';

-- E.164 제약조건 추가
DO
$$
  BEGIN
    IF NOT EXISTS(
      SELECT 1
      FROM pg_constraint
      WHERE conname = 'member_phone_e164_kr_check'
    ) THEN
      ALTER TABLE member
        ADD CONSTRAINT member_phone_e164_kr_check
          CHECK (phone ~ '^\+8210\d{8}$' OR phone IS NULL) NOT VALID;
    END IF;
  END;
$$;

ALTER TABLE member
  VALIDATE CONSTRAINT member_phone_e164_kr_check;

-- (deleted = false)에 대한 활성화 유니크 보장
-- 이미 존재하는 경우 skip
CREATE UNIQUE INDEX IF NOT EXISTS ux_member_phone_active
  ON member (phone)
  WHERE ((deleted = FALSE) AND (phone IS NOT NULL));
