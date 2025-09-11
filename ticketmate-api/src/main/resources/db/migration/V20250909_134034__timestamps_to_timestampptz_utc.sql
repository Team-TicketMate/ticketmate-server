-- 목적: 모든 timestamp(0) 컬럼을 timestamptz(0)로 전환
-- 기존 KST 로컬시각을 UTC 기준 시각으로 변환

SET search_path TO public;

-- 1) member
ALTER TABLE member
    ALTER COLUMN created_date    TYPE TIMESTAMPTZ(0) USING created_date    AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date    TYPE TIMESTAMPTZ(0) USING updated_date    AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN last_login_time TYPE TIMESTAMPTZ(0) USING last_login_time AT TIME ZONE 'Asia/Seoul';

-- 2) concert_hall
ALTER TABLE concert_hall
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 3) concert
ALTER TABLE concert
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 4) ticket_open_date
ALTER TABLE ticket_open_date
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN open_date    TYPE TIMESTAMPTZ(0) USING open_date    AT TIME ZONE 'Asia/Seoul';

-- 5) concert_date
ALTER TABLE concert_date
    ALTER COLUMN created_date     TYPE TIMESTAMPTZ(0) USING created_date     AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date     TYPE TIMESTAMPTZ(0) USING updated_date     AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN performance_date TYPE TIMESTAMPTZ(0) USING performance_date AT TIME ZONE 'Asia/Seoul';

-- 6) application_form
ALTER TABLE application_form
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 7) application_form_detail
ALTER TABLE application_form_detail
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 8) hope_area
ALTER TABLE hope_area
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 9) rejection_reason
ALTER TABLE rejection_reason
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 10) concert_agent_availability
ALTER TABLE concert_agent_availability
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 11) member_follow
ALTER TABLE member_follow
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 12) portfolio
ALTER TABLE portfolio
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 13) portfolio_img
ALTER TABLE portfolio_img
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 14) embedding
ALTER TABLE embedding
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';

-- 15) agent_performance_summary
ALTER TABLE agent_performance_summary
    ALTER COLUMN created_date TYPE TIMESTAMPTZ(0) USING created_date AT TIME ZONE 'Asia/Seoul',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ(0) USING updated_date AT TIME ZONE 'Asia/Seoul';