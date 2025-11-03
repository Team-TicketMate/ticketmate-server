-- account_status 값 변경 (ACTIVE_ACCOUNT, DELETE_ACCOUNT -> ACTIVE, WITHDRAWN, TEMP_BAN, PERMANENT_BAN)
-- 기존 UNIQUE 제약 조건 제거(username, nickname)
-- deleted = FALSE 범위에서만 username, nickname, phone 유니크 인덱스 생성 (부분 유니크)
-- 인덱스 생성 전 중복 점검

-- member 테이블 account_status
-- 기존 CHECK 제약조건 제거
ALTER TABLE member
  DROP CONSTRAINT IF EXISTS member_account_status_check;

-- 기존 값 -> 신규 값으로 매핑
UPDATE member
SET account_status = 'ACTIVE'
WHERE account_status = 'ACTIVE_ACCOUNT';

UPDATE member
SET account_status = 'WITHDRAWN'
WHERE account_status = 'DELETE_ACCOUNT';

-- 신규 CHECK 제약조건 추가
ALTER TABLE member
  ADD CONSTRAINT member_account_status_check
    CHECK ( account_status IN ('ACTIVE', 'WITHDRAWN', 'TEMP_BAN', 'PERMANENT_BAN') );

-- username, nickname, phone 유니크 인덱스
-- 기존 UNIQUE 제약조건 제거
ALTER TABLE member
  DROP CONSTRAINT IF EXISTS member_username_key;
ALTER TABLE member
  DROP CONSTRAINT IF EXISTS member_nickname_key;

-- 부분 유니크 인덱스 생성 전, 중복 사전 검증 (deleted = FALSE 범위)
DO
$$
  BEGIN
    IF EXISTS(
      SELECT username
      FROM member
      WHERE deleted = FALSE
        AND username IS NOT NULL
      GROUP BY username
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: 중복 username 존재 - deleted = FALSE 범위에서 유니크 인덱스 생성 불가';
    END IF;

    IF EXISTS(
      SELECT nickname
      FROM member
      WHERE deleted = FALSE
        AND nickname IS NOT NULL
      GROUP BY nickname
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: 중복 nickname 존재 - deleted = FALSE 범위에서 유니크 인덱스 생성 불가';
    END IF;

    IF EXISTS(
      SELECT phone
      FROM member
      WHERE deleted = FALSE
        AND phone IS NOT NULL
      GROUP BY phone
      HAVING COUNT(*) > 1
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: 중복 phone 존재 - deleted = FALSE 범위에서 유니크 인덱스 생성 불가';
    END IF;
  END;
$$;

-- username, nickname, phone 부분 유니크 인덱스 생성 (deleted = FALSE)
CREATE UNIQUE INDEX IF NOT EXISTS ux_member_username_active
  ON member (username)
  WHERE deleted = FALSE AND username IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_member_nickname_active
  ON member (nickname)
  WHERE deleted = FALSE AND nickname IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_member_phone_active
  ON member (phone)
  WHERE deleted = FALSE AND phone IS NOT NULL;