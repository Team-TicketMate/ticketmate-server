-- 1) 테이블 생성
CREATE TABLE IF NOT EXISTS public.agent_bank_account
(
    agent_bank_account_id UUID           NOT NULL PRIMARY KEY,
    agent_member_id       UUID           NOT NULL,
    bank_code             VARCHAR(255)   NOT NULL,
    bank_name             VARCHAR(255)   NOT NULL,
    account_holder        VARCHAR(64)    NOT NULL,
    account_number_enc    VARCHAR(256)   NOT NULL,
    primary_account       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_date          TIMESTAMPTZ(0) NOT NULL DEFAULT NOW(),
    updated_date          TIMESTAMPTZ(0) NOT NULL DEFAULT NOW()
);

-- 2 BankCode 제약
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='agent_bank_account_bank_code_check') THEN
    ALTER TABLE public.agent_bank_account
      ADD CONSTRAINT agent_bank_account_bank_code_check
      CHECK ( bank_code::text = ANY (ARRAY[
        'KYONGNAM_BANK','GWANGJU_BANK','LOCALNONGHYEOP','BUSAN_BANK','SAEMAUL','SANLIM',
        'SHINHAN','SHINHYEOP','CITI','WOORI','POST','SAVING_BANK','JEONBUK_BANK','JEJU_BANK',
        'KAKAO_BANK','K_BANK','TOSS_BANK','HANA','HSBC','IBK','KOOKMIN','DAEGU_BANK',
        'KDB_BANK','NONGHYEOP','SC','SUHYEOP'
      ]::text[]) );
  END IF;
END $$;