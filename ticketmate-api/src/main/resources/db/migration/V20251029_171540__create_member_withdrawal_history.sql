-- member_withdrawal_history 테이블 생성

CREATE TABLE member_withdrawal_history
(
  member_withdrawal_history_id UUID PRIMARY KEY,
  member_id                    UUID           NOT NULL,
  phone                        VARCHAR(13)    NOT NULL,
  nickname                     VARCHAR(12)    NOT NULL,
  withdrawal_reason_type       VARCHAR(255)   NOT NULL
    CONSTRAINT withdrawal_reason_type_check
      CHECK ( withdrawal_reason_type IN ('NO_CONCERTS', 'RUDE_USER', 'UNFAIR_RESTRICTION', 'WANT_NEW_ACCOUNT', 'DELETE_PERSONAL_DATA', 'OTHER') ),
  other_reason                 varchar(20),

  created_date                 timestamptz(0) NOT NULL,
  updated_date                 timestamptz(0) NOT NULL,
  deleted                      BOOLEAN        NOT NULL DEFAULT FALSE,
  deleted_date                 timestamptz(0)
);

CREATE INDEX IF NOT EXISTS idx_mwh_member_id ON member_withdrawal_history (member_id);
CREATE INDEX IF NOT EXISTS idx_mwh_phone ON member_withdrawal_history (phone);
