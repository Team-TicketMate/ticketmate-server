-- priority 1~5 제한조건 변경
-- 기존: 1~10
-- 변경: 1~5

-- priority 데이터가 1~5 범위를 벗어나는 경우 마이그레이션 중단
DO
$$
  DECLARE
    v_cnt bigint;
  BEGIN
    SELECT COUNT(*)
    INTO v_cnt
    FROM hope_area
    WHERE priority < 1
       OR priority > 5;
    IF v_cnt > 0 THEN
      RAISE EXCEPTION 'Flyway 마이그레이션 에러: hope_area.priority 컬럼에 1~5 범위를 벗어나는 값(개수: %)이 존재합니다.', v_cnt;
    END IF;
  END;
$$;

-- 기존 CHECK 제약조건 삭제 (1~10제한)
ALTER TABLE hope_area
  DROP CONSTRAINT IF EXISTS hope_area_priority_check;

-- 새로운 CHECK 제약조건 추가 (1~5제한)
ALTER TABLE hope_area
  ADD CONSTRAINT hope_area_priority_check
    CHECK ( priority >= 1 AND priority <= 5 );
