-- agent_bank_account 테이블 agent_member_id FK 추가

-- 기존 데이터 검증
-- agent_bank_account.agent_member_id 가 member.member_id를 참조하지 않는 행이 있으면 오류
DO
$$
  BEGIN
    IF EXISTS(
      SELECT 1
      FROM agent_bank_account aba
             LEFT JOIN member m
                       ON aba.agent_member_id = m.member_id
      WHERE m.member_id IS NULL
    ) THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: agent_bank_account.agent_member_id 에 잘못된 데이터가 있습니다.';
    END IF;
  END;
$$;

-- FK 제약조건 추가 (이미 존재하면 skip)
DO
$$
  BEGIN
    IF NOT EXISTS(
      SELECT 1
      FROM pg_constraint con
             JOIN pg_class cla ON con.conrelid = cla.oid
      WHERE con.conname = 'fk_agent_bank_account_member'
        AND cla.relname = 'agent_bank_account'
    ) THEN
      ALTER TABLE agent_bank_account
        ADD CONSTRAINT fk_agent_bank_account_member
          FOREIGN KEY (agent_member_id)
            REFERENCES member (member_id);
    END IF;
  END;
$$;

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_agent_bank_account_agent_member_id
  ON agent_bank_account (agent_member_id);
