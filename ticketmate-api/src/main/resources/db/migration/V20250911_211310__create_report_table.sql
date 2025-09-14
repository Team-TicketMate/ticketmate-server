-- Report(신고) 테이블 생성
CREATE TABLE public.report
(
    report_id               UUID           NOT NULL PRIMARY KEY,
    reporter_member_id      UUID           NOT NULL
        CONSTRAINT FK4u358afc7n9mo843563w364lp REFERENCES member,
    reported_member_member_id UUID           NOT NULL
        CONSTRAINT FKcdhkgbi9qbj1gnpqg2o1f3ido REFERENCES member,
    report_reason           VARCHAR(255)   NOT NULL
        CONSTRAINT report_reason_check
            CHECK ((report_reason)::TEXT = ANY ((ARRAY ['INAPPROPRIATE_LANGUAGE'::CHARACTER VARYING, 'FAKE_LISTING'::CHARACTER VARYING, 'SPAM'::CHARACTER VARYING])::TEXT[])),
    description             VARCHAR(200),
    report_status           VARCHAR(255)   NOT NULL
        CONSTRAINT report_status_check
            CHECK ((report_status)::TEXT = ANY ((ARRAY ['PENDING'::CHARACTER VARYING, 'IN_PROGRESS'::CHARACTER VARYING, 'RESOLVED'::CHARACTER VARYING, 'REJECTED'::CHARACTER VARYING])::TEXT[])),
    created_date     TIMESTAMPTZ(0) NOT NULL,
    updated_date     TIMESTAMPTZ(0) NOT NULL
);
