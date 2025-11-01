-- 대리인 마이페이지의 공연 관리 API 성능 개선을 위한 인덱스

-- '모집 여부' 서브쿼리 확인 시 사용
CREATE INDEX IF NOT EXISTS idx_ticket_open_date_concert_date
    ON public.ticket_open_date (concert_concert_id, open_date);

-- '매칭된 의뢰인 수' 서브쿼리 계산 시 사용
CREATE INDEX IF NOT EXISTS idx_app_form_concert_agent_status
    ON public.application_form (agent_member_id, concert_concert_id)
    WHERE application_form_status = 'APPROVED';