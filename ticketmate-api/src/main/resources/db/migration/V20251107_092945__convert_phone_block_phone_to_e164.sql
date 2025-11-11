-- 검증: 기존 데이터 중 숫자만 추출했을 때 010으로 시작하는 11자리가 아니면 예외 발생

DO
$$
  BEGIN
    IF EXISTS (
      SELECT 1
      FROM phone_block
      WHERE phone NOT LIKE '+82%' -- 이미 E.164 로 저장된 값은 검증 대상에서 제외
        AND REGEXP_REPLACE(phone, '[^0-9]', '', 'g') !~ '^010[0-9]{8}$'
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: 010으로 시작하지 않는 데이터가 있습니다.';
    END IF;
  END;
$$;

-- 변환: 010-1234-5678 -> +821012345678
UPDATE phone_block
SET phone = '+82' || SUBSTRING(REGEXP_REPLACE(phone, '[^0-9]', '', 'g') FROM 2)
WHERE phone NOT LIKE '+82%'
  AND REGEXP_REPLACE(phone, '[^0-9]', '', 'g') ~ '^010[0-9]{8}$';

-- E.164 제약조건 추가
ALTER TABLE phone_block
  ADD CONSTRAINT phone_block_phone_e164_kr_check
    CHECK (phone ~ '^\+8210[0-9]{8}$');
