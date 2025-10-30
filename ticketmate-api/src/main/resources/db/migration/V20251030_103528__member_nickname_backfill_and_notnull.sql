-- member 테이블 nickname not null 변경
-- 기존에 nickname이 없는 경우 12자리 랜덤 문자열 부여


-- 닉네임이 NULL 인 행에 12자리 랜덤 닉네임 부여
UPDATE member
SET nickname = LOWER(SUBSTR(REPLACE(member_id::text, '-', ''), 1, 12))
WHERE nickname IS NULL;

-- 검증: 여전히 NULL 이 남아있다면 에러
DO
$$
  BEGIN
    IF EXISTS(
      SELECT 1
      FROM member
      WHERE nickname IS NULL
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 오류: member 테이블 내부 nickname이 NULL 인 행이 남아있습니다.';
    END IF;
  END;
$$;

-- nickname column NOT NULL 전환
ALTER TABLE member
  ALTER COLUMN nickname SET NOT NULL;
