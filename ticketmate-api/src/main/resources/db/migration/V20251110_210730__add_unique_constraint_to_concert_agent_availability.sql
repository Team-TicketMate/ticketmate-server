-- 중복 데이터 존재 여부 검증
DO
$$
    DECLARE
    v_count BIGINT;
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM (
                 SELECT 1
                 FROM public.concert_agent_availability
                 GROUP BY concert_concert_id, agent_member_id
                 HAVING COUNT(*) > 1
             ) AS duplicate_check;

        IF v_count > 0 THEN
                    RAISE EXCEPTION
                        'Flyway 마이그레이션 에러: concert_agent_availability 테이블에 중복되는 (concert_concert_id, agent_member_id) 조합이 %개 존재합니다.', v_count;
        END IF;
    END;
$$;

-- concert, agent 유니크 인덱스 생성
CREATE UNIQUE INDEX ux_concert_agent_availability_concert_agent
    ON public.concert_agent_availability (concert_concert_id, agent_member_id);