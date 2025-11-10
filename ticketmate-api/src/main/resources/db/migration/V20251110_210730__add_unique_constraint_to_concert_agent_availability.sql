-- concert, agent 유니크 인덱스 생성
CREATE UNIQUE INDEX ux_concert_agent_availability_concert_agent
    ON public.concert_agent_availability (concert_concert_id, agent_member_id);