-- Report(신고) 테이블 생성
CREATE TABLE public.report
(
    report_id               UUID           NOT NULL PRIMARY KEY,
    reporter_member_id      UUID
        CONSTRAINT FK4u358afc7n9mo843563w364lp REFERENCES member,
    reported_user_member_id UUID
        CONSTRAINT FKcdhkgbi9qbj1gnpqg2o1f3ido REFERENCES member,
    reason           VARCHAR(255)   NOT NULL
        CONSTRAINT report_reason_check
            CHECK ((reason)::TEXT = ANY ((ARRAY ['INAPPROPRIATE_LANGUAGE'::CHARACTER VARYING, 'FAKE_LISTING'::CHARACTER VARYING, 'SPAM'::CHARACTER VARYING])::TEXT[])),
    description      TEXT,
    status           VARCHAR(255)   NOT NULL
        CONSTRAINT report_status_check
            CHECK ((status)::TEXT = ANY ((ARRAY ['PENDING'::CHARACTER VARYING, 'RESOLVED'::CHARACTER VARYING, 'REJECTED'::CHARACTER VARYING])::TEXT[])),
    created_date     TIMESTAMPTZ(0) NOT NULL,
    updated_date     TIMESTAMPTZ(0) NOT NULL
);
