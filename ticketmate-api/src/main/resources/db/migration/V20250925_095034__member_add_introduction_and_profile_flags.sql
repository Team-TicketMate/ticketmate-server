-- 1. 한줄소개 column 추가
-- 2. 본인인증 여부 column 추가
-- 3. 기본 프로필 설정 여부 column 추가

-- 기존 데이터 백필 규칙 적용

-- 컬럼 추가
ALTER TABLE member
  ADD COLUMN IF NOT EXISTS introduction varchar(50);

ALTER TABLE member
  ADD COLUMN IF NOT EXISTS is_phone_number_verified BOOLEAN;

ALTER TABLE member
  ADD COLUMN IF NOT EXISTS is_initial_profile_set BOOLEAN;

-- 기존 데이터 백필
UPDATE member
SET introduction = NULL
WHERE introduction IS NOT NULL
  AND BTRIM(introduction) = '';

UPDATE member
SET is_phone_number_verified = FALSE
WHERE is_phone_number_verified IS NULL;

UPDATE member
SET is_initial_profile_set = FALSE
WHERE is_initial_profile_set ISNULL;

-- 제약 및 기본값 설정
-- 신규 레코드 기본값 FALSE로 설정되도록 DEFAULT 추가
ALTER TABLE member
  ALTER COLUMN is_phone_number_verified SET DEFAULT FALSE;

ALTER TABLE member
  ALTER COLUMN is_phone_number_verified SET NOT NULL;

ALTER TABLE member
  ALTER COLUMN is_initial_profile_set SET DEFAULT FALSE;

ALTER TABLE member
  ALTER COLUMN is_initial_profile_set SET NOT NULL;
