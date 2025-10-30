-- member 테이블 nickname, birth_day, birth_year, phone 길이 제약조건 추가
-- nickname: 2 ~ 12자리
-- birth_day: 4자리
-- birth_year: 4자리
-- phone: 13자리

-- 사전 검증: 기존 데이터가 제약조건 위반 시 예외
DO
$$
  BEGIN
    -- 닉네임: NOT NULL + 2~12자
    IF EXISTS(
      SELECT 1
      FROM member
      WHERE CHAR_LENGTH(nickname) < 2
         OR CHAR_LENGTH(nickname) > 12
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: nickname은 2~12자 사이의 값만 허용됩니다. 기존 데이터 중 해당 제약조건을 위반하는 데이터가 있습니다.';
    END IF;

    -- 생일: NULL 허용 + 값이 있으면 정확히 4자 (MMDD)
    IF EXISTS(
      SELECT 1
      FROM member
      WHERE birth_day IS NOT NULL
        AND CHAR_LENGTH(birth_day) <> 4
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: brith_day는 정확히 4자 또는 NULL만 허용됩니다. 기존 데이터 중 해당 제약조건을 위반하는 데이터가 있습니다';
    END IF;

    -- 출생연도: NULL 허용 + 값이 있으면 정확히 4자 (YYYY)
    IF EXISTS(
      SELECT 1
      FROM member
      WHERE birth_year IS NOT NULL
        AND CHAR_LENGTH(birth_year) <> 4
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: brith_year는 정확히 4자 또는 NULL만 허용됩니다. 기존 데이터 중 해당 제약조건을 위반하는 데이터가 있습니다';
    END IF;

    -- 전화번호: NULL 허용 + 값이 있으면 정확히 13자 (010-1234-5678)
    IF EXISTS(
      SELECT 1
      FROM member
      WHERE phone IS NOT NULL
        AND CHAR_LENGTH(phone) <> 13

    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: phone은 정확히 13자 또는 NULL만 허용됩니다. 기존 데이터 중 해당 제약조건을 위반하는 데이터가 있습니다.';
    END IF;
  END;
$$;

-- 컬럼 타입 길이 축소
ALTER TABLE member
  ALTER COLUMN nickname TYPE varchar(12),
  ALTER COLUMN birth_day TYPE varchar(4),
  ALTER COLUMN birth_year TYPE varchar(4),
  ALTER COLUMN phone TYPE varchar(13);

-- 기존 길이 관련 CHECK 제약 제거 후 재생성 (존재할 수 있으니 drop 수행)
ALTER TABLE member
  DROP CONSTRAINT IF EXISTS member_nickname_len_check,
  DROP CONSTRAINT IF EXISTS member_birth_day_len_check,
  DROP CONSTRAINT IF EXISTS member_birth_year_len_check,
  DROP CONSTRAINT IF EXISTS member_phone_len_check;

-- 길이 관련 CHECK 제약 조건 추가
-- 닉네임: 반드시 2~12자
ALTER TABLE member
  ADD CONSTRAINT member_nickname_len_check
    CHECK (CHAR_LENGTH(nickname) BETWEEN 2 AND 12);

-- 생일: NULL 허용, 값이 있으면 정확히 4자
ALTER TABLE member
  ADD CONSTRAINT member_birth_day_len_check
    CHECK (birth_day IS NULL OR CHAR_LENGTH(birth_day) = 4);

-- 출생연도: NULL 허용, 값이 있으면 정확히 4자
ALTER TABLE member
  ADD CONSTRAINT member_birth_year_len_check
    CHECK (birth_year IS NULL OR CHAR_LENGTH(birth_year) = 4);

-- 전화번호: NULL 허용, 값이 있으면 정확히 13자
ALTER TABLE member
  ADD CONSTRAINT member_phone_len_check
    CHECK (phone IS NULL OR CHAR_LENGTH(phone) = 13);