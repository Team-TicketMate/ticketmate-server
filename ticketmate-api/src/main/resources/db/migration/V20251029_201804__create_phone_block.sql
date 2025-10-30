-- phone_block 테이블 생성

CREATE TABLE phone_block
(
  phone_block_id UUID PRIMARY KEY,
  phone          varchar(13)    NOT NULL UNIQUE,
  block_type     varchar(255)   NOT NULL
    CONSTRAINT block_type_check
      CHECK ( block_type IN ('WITHDRAWAL', 'TEMP_BAN', 'PERMANENT_BAN') ),
  blocked_until  timestamptz(0),

  created_date   timestamptz(0) NOT NULL,
  updated_date   timestamptz(0) NOT NULL,
  deleted        BOOLEAN        NOT NULL DEFAULT FALSE,
  deleted_date   timestamptz(0)
);

-- 성능 개선을 위한 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_phone_block_phone ON phone_block (phone);
CREATE INDEX IF NOT EXISTS idx_phone_block_blocked_until ON phone_block (blocked_until);
CREATE INDEX IF NOT EXISTS idx_phone_block_block_type ON phone_block (block_type);